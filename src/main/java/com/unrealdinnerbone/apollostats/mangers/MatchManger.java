package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.apollostats.api.GameState;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.apiutils.WebResultException;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.json.api.JsonParseException;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCPing;
import com.unrealdinnerbone.unreallib.web.HttpHelper;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchManger {
    private static final Logger LOGGER = LogHelper.getLogger();
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
                getUpcomingMatches().stream().filter(Match::isApolloGame).forEach(match -> {
                    if (match.removed()) {
                        if (trackedMatches.containsKey(match.id())) {
                            AlertManager.gameRemoved(match);
                            trackedMatches.get(match.id()).cancel();
                            trackedMatches.remove(match.id());
                            LOGGER.info("Removed match {}", match);
                        }
                    } else {
                        if (!trackedMatches.containsKey(match.id())) {
                            LOGGER.info("Scheduling match {}", match);
                            AlertManager.gameFound(match);
                            TimerTask timerTask = TaskScheduler.scheduleTask(Instant.parse(match.opens()), theTask -> watchForFill(match));
                            trackedMatches.put(match.id(), timerTask);
                        }
                    }
                });

                if(!upcoming.isDone()) {
                    upcoming.complete(null);
                }

            }, upcoming::completeExceptionally);
        }else {
            upcoming.complete(null);
        }
        return TaskScheduler.allAsync(List.of(staffTracker, upcoming));
    }

    private static void loadStaffMatchBacklog() {
        for(Staff staff : StaffManager.getStaff()) {
            List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
            LOGGER.info("Loaded {} matches for {}", matches.size(), staff.displayName());
            matchesMap.put(staff, matches);
        }
    }

    public static List<Match> getAllMatchesForHost(Staff staff, Optional<Integer> before) {
        List<Match> matches = new ArrayList<>();
        try {
            String json = HttpHelper.DEFAULT.getOrThrow("https://hosts.uhc.gg/api/hosts/" + staff.username().replace(" ", "%20/") + "/matches?count=50" + before.map(i -> "&before=" + i).orElse(""));
            List<Match> foundMatches = JsonUtil.DEFAULT.parseList(Match[].class, json);
            matches.addAll(foundMatches);
            if(foundMatches.size() == 50) {
                matches.addAll(getAllMatchesForHost(staff, Optional.of(matches.get(49).id())));
            }
        }catch(JsonParseException e) {
            LOGGER.error("Error while parsing json", e);
        }catch (WebResultException e) {
            LOGGER.error("Error while getting matches for {}", staff.displayName(), e);
        }
        return matches;
    }

    public static List<Match> getUpcomingMatches() throws Exception {
        String json = HttpHelper.DEFAULT.get("https://hosts.uhc.gg/api/matches/upcoming").body();
        return JsonUtil.DEFAULT.parseList(Match[].class, json);
    }

    public static TimerTask watchForFill(Match match) {
        LOGGER.warn("Watching for fill for match {}", match.id());
        AtomicBoolean hasGoneFromIdol = new AtomicBoolean(false);
        AtomicInteger fill = new AtomicInteger(0);
        return TaskScheduler.scheduleRepeatingTask(30, TimeUnit.SECONDS, task ->
                MCPing.ping(Stats.CONFIG.getServerIp(), Stats.CONFIG.getServerPort()).whenComplete((result, throwable) -> {
                    if (throwable == null) {
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
                        switch (state) {
                            case LOBBY -> hasGoneFromIdol.set(true);
                            case PRE_PVP -> {
                                hasGoneFromIdol.set(true);
                                int online = result.players().online();
                                if (online > fill.get()) {
                                    LOGGER.info("New Fill {} for {}", online, match.id());
                                    fill.set(online);
                                }
                            }
                            case PVP, MEATUP, IDLE -> {
                                if (state != GameState.IDLE) {
                                    hasGoneFromIdol.set(true);
                                }
                                if (hasGoneFromIdol.get()) {
                                    int totalFill = (fill.get() == 0 ? result.players().online() : fill.get()) - 1;
                                    LOGGER.info("Game {} fill is {}", match.id(), totalFill);
                                    Game game = new Game(match.id(), totalFill);
                                    if (!GameManager.getGames().contains(game)) {
                                        AlertManager.gameSaved(match, game);
                                        GameManager.addGames(Collections.singletonList(new Game(match.id(), totalFill)));
                                    } else {
                                        LOGGER.error("Game {} already exists", match.id());
                                    }
                                }
                                task.cancel();
                            }
                        }
                        LOGGER.info("Server State: {} Has Opened {}", state, hasGoneFromIdol.get());
                    } else {
                        LOGGER.error("Error while pinging", throwable);
                    }
                }));
    }

    public static Map<Staff, List<Match>> getMap() {
        return matchesMap;
    }

}