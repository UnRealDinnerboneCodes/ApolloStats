package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCPing;
import com.unrealdinnerbone.unreallib.web.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MatchManger {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchManger.class);

    private static final Map<Staff, List<Match>> matchesMap = new HashMap<>();

    private static final List<Integer> ids = new ArrayList<>();

    public static CompletableFuture<Void> init() {
        CompletableFuture<Void> future = new CompletableFuture<>();
            TaskScheduler.scheduleRepeatingTaskExpectantly(15, TimeUnit.MINUTES, task -> {
                loadMatchData();
                if(!future.isDone()) {
                    future.complete(null);
                }
            }, e -> LOGGER.error("Error loading match data", e));
            return future;
    }


    private static void loadMatchData() throws Exception {
        for(Staff staff : StaffManager.getStaff()) {
            List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
            LOGGER.info("{} has {} matches", staff, matches.size());
            matchesMap.put(staff, matches);
            matches.stream()
                    .filter(Predicate.not(Match::hasPlayed))
                    .filter(Predicate.not(Match::removed))
                    .forEach(match -> {
                        if(!ids.contains(match.id())) {
                            Instant opens = Instant.parse(match.opens());
                            if(opens.isAfter(Util.utcNow())) {
                                ids.add(match.id());
                                LOGGER.info("Scheduling match {}", match);
                                TaskScheduler.scheduleTask(Instant.parse(match.opens()), theTask -> {
                                    watchForFill(match);
                                    ids.remove((Object) match.id());
                                });
                            }
                            if(opens.isBefore(Util.utcNow().plus(15, ChronoUnit.MINUTES))) {
                                if(!ids.contains(match.id())) {
                                    watchForFill(match);
                                }
                            }
                        }

                    });
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

    public static void watchForFill(Match match) {
        LOGGER.warn("Watching for fill for match {}", match.id());
        AtomicInteger fill = new AtomicInteger(0);
        TaskScheduler.scheduleRepeatingTask(30, TimeUnit.SECONDS, task -> {
            MCPing.ping(Stats.CONFIG.getServerIp(), Stats.CONFIG.getServerPort()).whenComplete((result, throwable) -> {
                if(throwable == null) {
                    String message = Util.getMotdMessage(result);
                    GameState state = GameState.getState(message);
                    if(state == GameState.PRE_PVP) {
                        int online = result.players().online();
                        if(online > fill.get()) {
                            LOGGER.info("{} players online, filling match {}", online, match.id());
                            fill.set(online);
                        }
                    }else if(state == GameState.PVP) {
                        int totalFill = (fill.get() == 0 ? result.players().online() : fill.get()) - 1;
                        LOGGER.info("Game {} fill is {}", match.id(), totalFill);
                        Game game = new Game(match.id(), totalFill);
                        if(!GameManager.getGames().contains(game)) {
                            AlertManager.gameSaved(match, game);
                            GameManager.addGames(Collections.singletonList(new Game(match.id(), totalFill)));
                        }else {
                            LOGGER.error("Game {} already exists", match.id());
                        }
                        task.cancel();
                    }else if(state == GameState.IDLE || state == GameState.MEATUP) {
                        task.cancel();
                    }else {
                        LOGGER.info("Game {} is {} at {}", match.id(), state, result.players().online());
                    }
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
        LOBBY(s -> s.startsWith("Apollo » No game is running.\nWhitelist is off. Arena is")),
        PRE_PVP(s -> s.startsWith("Apollo » PvP is in: ")),
        PVP(s -> s.startsWith("Apollo » Meetup is in: ")),
        MEATUP(s -> s.startsWith("Apollo » Meetup is now!")),
//        OVER(s -> false),
        UNKNOWN(s -> false);
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

