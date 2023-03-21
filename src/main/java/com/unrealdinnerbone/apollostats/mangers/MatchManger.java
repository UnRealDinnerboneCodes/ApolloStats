package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.CachedStat;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.json.exception.JsonParseException;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCPing;
import com.unrealdinnerbone.unreallib.web.HttpHelper;
import org.slf4j.Logger;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchManger implements IManger {
    private static final Logger LOGGER = LogHelper.getLogger();
    private final Map<Staff, List<Match>> matchesMap = new HashMap<>();
    private final Map<Integer, TimerTask> trackedMatches = new HashMap<>();


    private void loadStaffMatchBacklog(boolean all) {
        for(Staff staff : Stats.INSTANCE.getStaffManager().getStaff()) {
            if(all || staff.type() != Staff.Type.RETIRED) {
                List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
                LOGGER.info("Loaded {} matches for {}", matches.size(), staff.displayName());
                matchesMap.put(staff, matches);
            }
        }
    }

    public List<Match> getAllMatchesForHost(Staff staff, Optional<Integer> before) {
        List<Match> matches = new ArrayList<>();
        try {
            String json = HttpHelper.getOrThrow(URI.create("https://hosts.uhc.gg/api/hosts/" + staff.username().replace(" ", "%20/") + "/matches?count=50" + before.map(i -> "&before=" + i).orElse("")));
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

    public static List<Match> getUpcomingMatches() throws WebResultException {
        String json = HttpHelper.getOrThrow(URI.create("https://hosts.uhc.gg/api/matches/upcoming"));
        return JsonUtil.DEFAULT.parseList(Match[].class, json);
    }

    public TimerTask watchForFill(Match match) {
        LOGGER.warn("Watching for fill for match {}", match.id());
        AtomicBoolean hasGoneFromIdol = new AtomicBoolean(false);
        AtomicInteger fill = new AtomicInteger(0);
        return TaskScheduler.scheduleRepeatingTask(30, TimeUnit.SECONDS, task ->
                MCPing.ping(Stats.INSTANCE.getStatsConfig().getServerIp(), Stats.INSTANCE.getStatsConfig().getServerPort()).whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("Error while pinging", throwable);
                    } else {
                        String message = Util.getMotdMessage(result);

                        TaskScheduler.handleTaskOnThread(() -> {
                            Stats.INSTANCE.getPostgresHandler().executeUpdate("INSERT INTO public.ping (time, players, motd, game) VALUES (?, ?, ?, ?)", handler -> {
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
                                    if (!Stats.INSTANCE.getGameManager().getGames().contains(game)) {
                                        AlertManager.gameSaved(match, game);
                                        Stats.INSTANCE.getGameManager().addGames(Collections.singletonList(new Game(match.id(), totalFill)));
                                    } else {
                                        LOGGER.error("Game {} already exists", match.id());
                                    }
                                }
                                task.cancel();
                            }
                        }
                        LOGGER.info("Server State: {} Has Opened {}", state, hasGoneFromIdol.get());
                    }
                }));
    }

    public Map<Staff, List<Match>> getMap() {
        return matchesMap;
    }

    @Override
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> staffTracker = new CompletableFuture<>();
        TaskScheduler.scheduleRepeatingTaskExpectantly(1, TimeUnit.HOURS, task -> {
            loadStaffMatchBacklog(!staffTracker.isDone());
            if(!staffTracker.isDone()) {
                staffTracker.complete(null);
            }
        }, staffTracker::completeExceptionally);

        CompletableFuture<Void> upcoming = new CompletableFuture<>();

        if(Stats.INSTANCE.getStatsConfig().watchMatches()) {
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
                            match.findStaff().ifPresent(staff -> {
                                CachedStat.getCachedStats().forEach(cachedStat -> {
                                    cachedStat.clear(staff);
                                    cachedStat.clear(Staff.APOLLO);
                                });
                            });
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

    @Override
    public String getName() {
        return "Match Manger";
    }
}