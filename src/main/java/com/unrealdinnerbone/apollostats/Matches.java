package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCPing;
import com.unrealdinnerbone.unreallib.web.HttpUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Matches {
    private static final Logger LOGGER = LoggerFactory.getLogger(Matches.class);

    private static final Map<String, List<Match>> matchesMap = new HashMap<>();

    private static final List<Integer> ids = new ArrayList<>();

    public static void init(PostgressHandler postgressHandler) {
        Util.STAFF.stream().parallel().forEach(staff -> TaskScheduler.scheduleRepeatingTaskExpectantly(15, TimeUnit.MINUTES, task -> {
            List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
            LOGGER.info("{} has {} matches", staff, matches.stream().filter(Match::isApolloGame).filter(Predicate.not(Match::removed)).count());
            matchesMap.put(staff, matches);
            matches.stream().filter(Predicate.not(Match::hasPlayed))
                    .forEach(match -> {
                        if(!ids.contains(match.id())) {
                            Instant opens = Instant.parse(match.opens());
                            if(opens.isAfter(Instant.now())) {
                                ids.add(match.id());
                                LOGGER.info("Scheduling match {}", match.id());
                                TaskScheduler.scheduleTask(Instant.parse(match.opens()), theTask -> {
                                    watchForFill(postgressHandler, match);
                                    ids.remove(match.id());
                                });
                            }
                            if(opens.isBefore(Instant.now().plus(15, ChronoUnit.MINUTES))) {
                                if(!ids.contains(match.id())) {
                                    watchForFill(postgressHandler, match);
                                }
                            }
                        }

                    });

        }, e -> LOGGER.error("Failed to load matches for {}", staff, e)));
    }

    public static List<Match> getAllMatchesForHost(String name, Optional<Integer> before) throws Exception {
        String json = HttpUtils.get("https://hosts.uhc.gg/api/hosts/" + name.replace(" ", "%20/") + "/matches?count=50" + before.map(i -> "&before=" + i).orElse("")).body();
        try {
            List<Match> matches = Arrays.stream(JsonUtil.DEFAULT.parse(Match[].class, json)).collect(Collectors.toList());
            if(matches.size() == 50) {
                matches.addAll(getAllMatchesForHost(name, Optional.of(matches.get(49).id())));
            }
            return matches;
        }catch(AssertionError e) {
            LOGGER.error("Error while parsing json", e);
            return new ArrayList<>();
        }
    }

    public static void watchForFill(PostgressHandler postgressHandler, Match match) {
        LOGGER.warn("Watching for fill for match {}", match.id());
        AtomicInteger fill = new AtomicInteger(0);
        TaskScheduler.scheduleRepeatingTask(1, TimeUnit.MINUTES, task -> {
            MCPing.ping("apollouhc.com", 25565).whenComplete((result, throwable) -> {
                if(throwable == null) {
                    Component component = GsonComponentSerializer.gson().deserialize(result.description());
                    String message = PlainTextComponentSerializer.plainText().serialize(component);
                    GameState state = GameState.getState(message);
                    if(state == GameState.PVP) {
                        int online = result.players().online();
                        if(online > fill.get()) {
                            fill.set(online);
                        }
                    }else if(state == GameState.MEATUP) {
                        LOGGER.info("Match {} is full", match.id());
                        Games.addGames(postgressHandler, Collections.singletonList(new Game(match.id(), fill.get())));
                        task.cancel();
                    }
                    LOGGER.warn("Ping Data: {} ({})", message, state.name());
                }else {
                    LOGGER.error("Error while pinging", throwable);
                }
            });
        });

    }

    public static Map<String, List<Match>> getMap() {
        return matchesMap;
    }

    public enum GameState {
        IDLE(s -> s.equalsIgnoreCase("Apollo » No game is running.\nWhitelist is on.")),
        LOBBY(s -> s.startsWith("Apollo » No game is running.\nWhitelist is off. Arena is")),
        PVP(s -> s.startsWith("Apollo » PvP is in: ")),
        MEATUP(s -> false),
        OVER(s -> false),
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

