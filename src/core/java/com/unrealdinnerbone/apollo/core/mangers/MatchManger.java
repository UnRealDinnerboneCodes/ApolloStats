package com.unrealdinnerbone.apollo.core.mangers;

import com.unrealdinnerbone.apollo.core.ApolloEventManager;
import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.*;
import com.unrealdinnerbone.apollo.core.api.event.MatchEvents;
import com.unrealdinnerbone.apollo.core.lib.CachedStat;
import com.unrealdinnerbone.apollo.core.lib.Util;
import com.unrealdinnerbone.apollo.core.stats.StatTypes;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.apiutils.APIUtils;
import com.unrealdinnerbone.unreallib.apiutils.result.IResult;
import com.unrealdinnerbone.unreallib.apiutils.result.JsonResult;
import com.unrealdinnerbone.unreallib.exception.ExceptionRunnable;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.json.exception.JsonParseException;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCPing;
import com.unrealdinnerbone.unreallib.web.HttpHelper;
import org.jetbrains.annotations.NotNull;
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


    private void loadStaffMatchBacklog() {
        for(Staff staff : Stats.INSTANCE.getStaffManager().getStaff()) {
            List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
            LOGGER.info("Loaded {} matches for {}", matches.size(), staff.displayName());
            matchesMap.put(staff, matches);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public List<Match> getAllMatchesForHost(Staff staff, Optional<Integer> before) {
        List<Match> matches = new ArrayList<>();
        try {
            JsonResult<Match[]> result = APIUtils.getJson(Match[].class, "https://hosts.uhc.gg/api/hosts/" + staff.username().replace(" ", "%20/") + "/matches?count=50" + before.map(i -> "&before=" + i).orElse(""));
            Match[] foundMatches = result.getNow();
            matches.addAll(Arrays.asList(foundMatches));
            if(foundMatches.length == 50) {
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
                MCPing.ping(Stats.INSTANCE.getStatsConfig().getServerIp(), Stats.INSTANCE.getStatsConfig().getServerPort())
                        .whenComplete((result, throwable) -> {
                            if(throwable != null) {
                                LOGGER.error("Error while pinging", throwable);
                            } else {
                                String message = Util.getMotdAsString(result);

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
                                        if(online > fill.get()) {
                                            LOGGER.info("New Fill {} for {}", online, match.id());
                                            fill.set(online);
                                        }
                                    }
                                    case PVP, MEATUP, IDLE -> {
                                        if(state != GameState.IDLE) {
                                            hasGoneFromIdol.set(true);
                                        }
                                        if(hasGoneFromIdol.get()) {
                                            int totalFill = (fill.get() == 0 ? result.players().online() : fill.get()) - 1;
                                            LOGGER.info("Game {} fill is {}", match.id(), totalFill);
                                            Game game = new Game(match.id(), totalFill);
                                            if(!Stats.INSTANCE.getGameManager().getGames().contains(game)) {
                                                ApolloEventManager.EVENT_MANAGER.post(new MatchEvents.GameSaved(match, game));
                                                Stats.INSTANCE.getGameManager().addGame(game);
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
    public void start() {
        loadStaffMatchBacklog();


        if(Stats.INSTANCE.getStatsConfig().watchMatches()) {
            TaskScheduler.scheduleRepeatingTaskExpectantly(1, TimeUnit.MINUTES, task -> {
                getUpcomingMatches()
                        .stream()
                        .filter(Match::isApolloGame)
                        .forEach(match -> {
                            if(match.removed()) {
                                if(trackedMatches.containsKey(match.id())) {
                                    ApolloEventManager.EVENT_MANAGER.post(new MatchEvents.GameRemoved(match));
                                    trackedMatches.get(match.id()).cancel();
                                    trackedMatches.remove(match.id());
                                    LOGGER.info("Removed match {}", match);
                                }
                            } else {
                                if(!trackedMatches.containsKey(match.id())) {
                                    LOGGER.info("Scheduling match {}", match);
                                    ApolloEventManager.EVENT_MANAGER.post(new MatchEvents.GameFound(match));
                                    StatTypes.clearCache();
                                    TimerTask timerTask = TaskScheduler.scheduleTask(Instant.parse(match.opens()), theTask -> watchForFill(match));
                                    trackedMatches.put(match.id(), timerTask);
                                }
                            }
                        });


            }, e -> LOGGER.error("Error while watching for matches", e));
        }
    }

}