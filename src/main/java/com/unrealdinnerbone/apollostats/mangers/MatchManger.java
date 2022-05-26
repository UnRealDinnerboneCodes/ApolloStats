package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.postgresslib.PostgresConsumer;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.exception.ExceptionConsumer;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCPing;
import com.unrealdinnerbone.unreallib.web.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MatchManger {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchManger.class);

    private static final Map<Staff, List<Match>> matchesMap = new HashMap<>();

    private static final Map<Integer, TimerTask> trackedMatches = new HashMap<>();

    public static CompletableFuture<Void> init() {
        CompletableFuture<Void> staffTracker = new CompletableFuture<>();
        TaskScheduler.scheduleRepeatingTaskExpectantly(1, TimeUnit.HOURS, task -> {
            loadStaffMatchBacklog();




            if(!staffTracker.isDone()) {
                staffTracker.complete(null);
            }
        }, staffTracker::completeExceptionally);

        CompletableFuture<Void> upcoming = new CompletableFuture<>();

        if(Stats.CONFIG.watchMatches()) {
            TaskScheduler.scheduleRepeatingTaskExpectantly(1, TimeUnit.MINUTES, task -> {
                List<Match> matches = getUpcomingMatches().stream().filter(Match::isApolloGame).toList();
                for(Match match : matches) {
                    if(match.removed()) {
                        if(trackedMatches.containsKey(match.id())) {
                            AlertManager.gameRemoved(match);
                            trackedMatches.get(match.id()).cancel();
                            trackedMatches.remove(match.id());
                            LOGGER.info("Removed match {}", match);
                        }else {
                            LOGGER.info("Match {} was removed but not tracked", match);
                        }
                    }else {
                        if(!trackedMatches.containsKey(match)) {
                            LOGGER.info("Scheduling match {}", match);
                            AlertManager.gameFound(match);
                            TimerTask timerTask = TaskScheduler.scheduleTask(Instant.parse(match.opens()), theTask -> watchForFill(match));
                            trackedMatches.put(match.id(), timerTask);
                        }
                    }
                }

                if(!upcoming.isDone()) {
                    upcoming.complete(null);
                }

            }, upcoming::completeExceptionally);
        }else {
            upcoming.complete(null);
        }
        return TaskScheduler.allAsync(List.of(staffTracker, upcoming));
    }

    private static void loadStaffMatchBacklog() throws Exception {
        for(Staff staff : StaffManager.getStaff()) {
            List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
            LOGGER.info("Loaded {} matches for {}", matches.size(), staff.displayName());
            matchesMap.put(staff, matches);
        }
    }

    public static List<Match> getAllMatchesForHost(Staff staff, Optional<Integer> before) throws Exception {
        String json = HttpUtils.get("https://hosts.uhc.gg/api/hosts/" + staff.username().replace(" ", "%20/") + "/matches?count=50" + before.map(i -> "&before=" + i).orElse("")).body();
        try {
            List<Match> matches = Arrays.stream(JsonUtil.DEFAULT.parse(Match[].class, json)).collect(Collectors.toList());
            if(matches.size() == 50) {
                matches.addAll(getAllMatchesForHost(staff, Optional.of(matches.get(49).id())));
            }
            return matches;
        }catch(AssertionError e) {
            LOGGER.error("Error while parsing json", e);
            return Collections.emptyList();
        }
    }

    public static List<Match> getUpcomingMatches() throws Exception {
        String json = HttpUtils.get("https://hosts.uhc.gg/api/matches/upcoming").body();
        return Arrays.stream(JsonUtil.DEFAULT.parse(Match[].class, json)).collect(Collectors.toList());
    }

    public static TimerTask watchForFill(Match match) {
        LOGGER.warn("Watching for fill for match {}", match.id());
        AtomicBoolean hasGoneFromIdol = new AtomicBoolean(false);
        AtomicInteger fill = new AtomicInteger(0);
        return TaskScheduler.scheduleRepeatingTask(30, TimeUnit.SECONDS, task -> {
            MCPing.ping(Stats.CONFIG.getServerIp(), Stats.CONFIG.getServerPort()).whenComplete((result, throwable) -> {
                if(throwable == null) {
                    String message = Util.getMotdMessage(result);

                    TaskScheduler.handleTaskOnThread(() -> {
                        Stats.getPostgresHandler().executeUpdate("INSERT INTO public.ping (time, players, motd, game) VALUES (?, ?, ?, ?)", handler -> {
                            handler.setLong(1, Instant.now().toEpochMilli() / 1000);
                            handler.setInt(2, result.players().online());
                            handler.setString(3, message);
                            handler.setInt(4, match.id());
                        });
                    });


                    GameState state = GameState.getState(message);
                    if(state == GameState.LOBBY) {
                        hasGoneFromIdol.set(true);
                    }
                    if(state == GameState.PRE_PVP) {
                        hasGoneFromIdol.set(true);
                        int online = result.players().online();
                        if(online > fill.get()) {
                            LOGGER.info("New Fill {} for {}", online, match.id());
                            fill.set(online);
                        }
                    }else if(state == GameState.PVP || state == GameState.MEATUP || state == GameState.IDLE) {
                        if(state != GameState.IDLE) {
                            hasGoneFromIdol.set(true);
                        }
                        if(hasGoneFromIdol.get()) {
                            int totalFill = (fill.get() == 0 ? result.players().online() : fill.get()) - 1;
                            LOGGER.info("Game {} fill is {}", match.id(), totalFill);
                            Game game = new Game(match.id(), totalFill);
                            if(!GameManager.getGames().contains(game)) {
                                AlertManager.gameSaved(match, game);
                                GameManager.addGames(Collections.singletonList(new Game(match.id(), totalFill)));
                            }else {
                                LOGGER.error("Game {} already exists", match.id());
                            }
                        }
                        task.cancel();
                    }else {
                        LOGGER.info("Game {} is {} at {}", match.id(), state, result.players().online());
                    }
                    LOGGER.info("Server State: {} Has Opened {}", state, hasGoneFromIdol.get());
                }else {
                    LOGGER.error("Error while pinging", throwable);
                }
            });
        });
    }

    public static Map<Staff, List<Match>> getMap() {
        return matchesMap;
    }

    public enum GameState {
        IDLE(s -> s.equalsIgnoreCase("Apollo » No game is running.\nWhitelist is on.")),
        LOBBY(s -> s.startsWith("Apollo » No game is running.") && s.contains("Arena is")),
        PRE_PVP(s -> s.startsWith("Apollo » PvP is in: ")),
        PVP(s -> s.startsWith("Apollo » Meetup is in: ")),
        MEATUP(s -> s.startsWith("Apollo » Meetup is now!")),
//        OVER(s -> false),
        UNKNOWN(s -> false),
        ;

        private final Predicate<String> matches;

        GameState(Predicate<String> matches) {
            this.matches = matches;
        }

        public boolean matches(String name) {
            return matches.test(name);
        }

        public static GameState getState(String name) {
            return Arrays.stream(values()).filter(state -> state.matches(name)).findFirst().orElse(UNKNOWN);
        }
    }
}

//    String statement = "INSERT INTO public.match (id, author, opens, address, ip, scenarios, tags, teams, size, custom_style, count, content, region, removed, removed_by, removed_reason, created, location, main_version, version, slots, length, mapSize, pvp_enabled_at, approved_by, hostingName, tournament, roles) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,? ,? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
//            " on conflict (id) do update removed = excluded.removed";
//    List<PostgresConsumer> postgresConsumers = new ArrayList<>();
//
//            matchesMap.values().stream().flatMap(Collection::stream).forEach(match -> {
//                    postgresConsumers.add(pStm -> {
//                    pStm.setString(1, match.ip());
//                    pStm.setString(2, match.author());
//                    pStm.setString(3, String.valueOf(Instant.parse(match.opens()).getEpochSecond()));
//                    pStm.setString(4, match.address());
//                    pStm.setString(5, match.ip());
//                    pStm.setString(6, String.join(";", match.scenarios()));
//                    pStm.setString(7, String.join(";", match.tags()));
//                    pStm.setString(8, String.join(";", match.teams()));
//                    pStm.setInt(9, match.size() != null ? match.size() : -1);
//                    pStm.setString(10, match.customStyle());
//                    pStm.setInt(11, match.count());
//                    pStm.setString(12, match.content());
//                    pStm.setString(13, match.region());
//                    pStm.setBoolean(14, match.removed());
//                    pStm.setString(15, match.removedBy());
//                    pStm.setString(16, match.removedReason());
//                    pStm.setString(17, String.valueOf(Instant.parse(match.created()).getEpochSecond()));
//                    pStm.setString(18, match.location());
//                    pStm.setString(19, match.mainVersion());
//                    pStm.setString(20, match.version());
//                    pStm.setInt(21, match.slots());
//                    pStm.setInt(22, match.length());
//                    pStm.setInt(23, match.mapSize());
//                    pStm.setInt(24, match.pvpEnabledAt());
//                    pStm.setString(25, match.approvedBy());
//                    pStm.setString(26, match.hostingName());
//                    pStm.setBoolean(27, match.tournament());
//                    pStm.setString(28, String.join(";", match.roles()));
//                    });
//
//                    });
//     TaskScheduler.handleTaskOnThread(() -> {
//             LOGGER.info("Saving {} matches to database", postgresConsumers.size());
//             try {
//             Stats.getPostgresHandler().executeBatchUpdate(statement, postgresConsumers);
//             }catch(Exception e){
//             LOGGER.error("Failed to save matches to database", e);
//             }
//             LOGGER.info("Saved {} matches to database", postgresConsumers.size());
//             });