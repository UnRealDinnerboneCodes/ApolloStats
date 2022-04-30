package com.unrealdinnerbone.apollostats;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.unrealdinnerbone.apollostats.api.BingoValue;
import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.web.ApolloRole;
import com.unrealdinnerbone.apollostats.web.Results;
import com.unrealdinnerbone.apollostats.web.WebAccessManger;
import com.unrealdinnerbone.apollostats.web.pages.other.RandomScenarioGenerator;
import com.unrealdinnerbone.apollostats.web.pages.stats.*;
import com.unrealdinnerbone.config.ConfigManager;
import com.unrealdinnerbone.postgresslib.PostgresConfig;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.discord.DiscordWebhook;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.web.HttpUtils;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Stats {
    private static final Logger LOGGER = LoggerFactory.getLogger("Stats");
    private static final List<IWebPage> statPages = new ArrayList<>();
    private static final Cache<String, String> pages = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
    private static final Cache<String, String> BINGO_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private static final List<BingoValue> BINGO_VALUES = new ArrayList<>();

    public static final Config CONFIG;
    private static final PostgresConfig POSTGRES_CONFIG;

    static {
        ConfigManager configManager = ConfigManager.createSimpleEnvPropertyConfigManger();
        CONFIG = configManager.loadConfig("apollo", Config::new);
        POSTGRES_CONFIG = configManager.loadConfig("postgres", PostgresConfig::new);
        statPages.add(new TopScenariosGen());
        statPages.add(new LastPlayedGen());
        statPages.add(new LongTimeGames());
        statPages.add(new TeamTypesGames());
        statPages.add(new TeamSizeGames());
        statPages.add(new TimeBetweenGames());
        statPages.add(new HostIn24HoursGen());
        statPages.add(new RandomScenarioGenerator());
        statPages.add(new Season9Thing());
    }

    public static void main(String[] args) throws Exception {
        PostgressHandler postgressHandler = new PostgressHandler(POSTGRES_CONFIG);
        ResultSet resultSet = postgressHandler.getSet("SELECT * FROM public.bingo");
        while(resultSet.next()) {
            BINGO_VALUES.add(new BingoValue(resultSet.getString("bingo"), resultSet.getBoolean("auto_win"), resultSet.getBoolean("is_player")));
        }

        Map<String, List<Match>> hostMatchMap = new HashMap<>();

        TaskScheduler.scheduleRepeatingTask(1, TimeUnit.HOURS, new TimerTask() {
            @Override
            public void run() {
                try {
                    Scenarios.loadDiskData();
                    hostMatchMap.clear();
                    for(String staff : Util.STAFF) {
                        List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
                        LOGGER.info("{} has {} matches", staff, matches.stream().filter(Match::isApolloGame).filter(Predicate.not(Match::removed)).count());
                        hostMatchMap.put(staff, matches);
                    }
                }catch(Exception e) {
                    LOGGER.error("Error while updating data", e);
                }
            }});

        Javalin publicPlace = Javalin.create(javalinConfig -> {
            javalinConfig.accessManager(new WebAccessManger());
            javalinConfig.addStaticFiles("/web", Location.CLASSPATH);
        }).start(1000);

        Javalin pushAPI = Javalin.create(javalinConfig -> javalinConfig.accessManager(new WebAccessManger())).start(1001);

        for(IWebPage generator : statPages) {
            publicPlace.get(generator.getName(), ctx -> {
                LOGGER.info("Received request for {}", generator.getName());
                ctx.html(pages.get(generator.getName(), () -> generator.generateStats(hostMatchMap, ctx::queryParam)));
            }, generator.getRole());
        }

        publicPlace.get("random_game/{id}", ctx -> ctx.html(RandomScenarioGenerator.generatePage(ctx.pathParam("id"), true)), ApolloRole.EVERYONE);


        pushAPI.post("/v1/bingo/add", ctx -> {
            try {
                BingoValue value = JsonUtil.DEFAULT.parse(BingoValue.class, ctx.body());
                if(value == null) {
                    ctx.status(HttpCode.BAD_REQUEST).result(Results.message("Invalid JSON (How did you get here?)"));
                }else {
                    if(value.bingo() == null) {
                        ctx.status(HttpCode.BAD_REQUEST).result(Results.message("Bingo value is null"));
                    }else {
                        if(BINGO_VALUES.contains(value)) {
                            ctx.status(HttpCode.BAD_REQUEST).result(Results.message("Bingo value already exists"));
                        }else {
                            BINGO_VALUES.add(value);
                            LOGGER.info("Added {}", value);
                            postgressHandler.executeUpdate("INSERT INTO public.bingo (bingo, auto_win, is_player) VALUES (?, ?, ?)", statement -> {
                                statement.setString(1, value.bingo());
                                statement.setBoolean(2, value.isBingo());
                                statement.setBoolean(3, value.isPlayer());
                            });
                            TaskScheduler.handleTaskOnThread(() -> {
                                try {
                                    DiscordWebhook.of(CONFIG.getDiscordWebBotToken()).setContent("Added new bingo value: " + value.bingo()).execute();
                                }catch(IOException | InterruptedException e) {
                                    LOGGER.error("Error while sending message to discord", e);
                                }
                            });
                            ctx.result(Results.message("Successfully added bingo value"));
                        }
                    }
                }
            }catch(Exception e) {
                ctx.status(HttpCode.BAD_REQUEST).result(Results.message(e.getMessage()));
            }

        }, ApolloRole.POST_API);


        pushAPI.get("/v1/bingo", ctx -> ctx.result(JsonUtil.DEFAULT.toJson(List.class, BINGO_VALUES)), ApolloRole.EVERYONE);


        publicPlace.get("bingo", ctx -> {
            String perm = ctx.queryParam("player");
            boolean isPlayer = perm == null || Boolean.parseBoolean(perm);
            String freeSpace = ctx.queryParam("freeSpace");
            ctx.html(getBingoCard(Util.createID(), postgressHandler, isPlayer, freeSpace == null ? "Cooldude Racism Mute": freeSpace));
        });
        publicPlace.get("bingo/{id}", ctx -> {
            String id = ctx.pathParam("id");
            String perm = ctx.queryParam("player");
            boolean isPlayer = perm == null || Boolean.parseBoolean(perm);
            String freeSpace = ctx.queryParam("freeSpace");
            ctx.html(getBingoCard(id, postgressHandler, isPlayer, freeSpace == null ? "Cooldude Racism Mute": freeSpace));
        });

    }


    public static String getBingoCard(String id, PostgressHandler postgressHandler, boolean players, String freeSpace) {
        Random random = new Random(id.hashCode());
        try {
            return BINGO_CACHE.get(id, () -> {
                ResultSet resultSet = postgressHandler.getSet("SELECT * FROM public.cards where id = ?", statement -> statement.setString(1, id));
                if(resultSet.next()) {
                    String values = resultSet.getString("values");
                    String theFreespace = resultSet.getString("freespace");
                    String[] split = values.split(";");
                    String[] frocedBingos = resultSet.getString("bingos").split(";");
                    return getCard(List.of(split), List.of(frocedBingos), theFreespace, "");
                }else {
                    List<BingoValue> values = new ArrayList<>(BINGO_VALUES.stream().filter(bingoValue -> players || bingoValue.isPlayer())
                            .toList());
                    if(values.size() >= 25) {
                        List<String> list = new ArrayList<>();
                        List<String> forced = new ArrayList<>();
                        for(int i = 0; i < 25; i++) {
                            BingoValue bingoValue = ArrayUtil.getRandomValueAndRemove(random, values);
                            list.add(bingoValue.bingo());
                            if(bingoValue.isBingo()) {
                                forced.add(bingoValue.bingo());
                            }
                        }
                        TaskScheduler.handleTaskOnThread(() -> {
                            try {
                                postgressHandler.executeUpdate("INSERT INTO public.cards (id, values, freespace, bingos) VALUES (?, ?, ?, ?)", statement -> {
                                    statement.setString(1, id);
                                    statement.setString(2, String.join(";", list));
                                    statement.setString(3, freeSpace);
                                    statement.setString(4, String.join(";", forced));
                                });
                            }catch(Exception e) {
                                LOGGER.error("Error while inserting bingo card", e);
                            }
                        });
                        return getCard(list, forced, freeSpace, id);

                    }else {
                        throw new RuntimeException("Not enough bingo values");
                    }
                }

            });
        }catch(ExecutionException | UncheckedExecutionException e) {
            LOGGER.error("Error getting bingo card", e);
            return getResourceAsString("/500.html");
        }
    }

    public static String getCard(List<String> values, List<String> forced, String freeSpace, String id) {
        return getResourceAsString("bingo.html")
                .replace("{\"BINGO_VALUES\"}", format(values))
                .replace("{\"BINGO_WORDS\"}", format(forced))
                .replace("{\"FREE_SPACE\"}", freeSpace)
                .replace("{\"URL\"}", id);
    }

    public static String format(List<String> values) {
        return values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
    }

    public static List<Match> getAllMatchesForHost(String name, Optional<Integer> before) throws Exception {
        String json = HttpUtils.get("https://hosts.uhc.gg/api/hosts/" + name.replace(" ", "%20/") + "/matches?count=50" + before.map(i -> "&before=" + i).orElse("")).body();
        try {
            List<Match> matches = Arrays.stream(JsonUtil.DEFAULT.parse(Match[].class, json)).collect(Collectors.toList());
            if (matches.size() == 50) {
                matches.addAll(getAllMatchesForHost(name, Optional.of(matches.get(49).id())));
            }
            return matches;
        }catch(AssertionError e) {
            LOGGER.error("Error while parsing json", e);
            return new ArrayList<>();
        }
    }


    public static String getResourceAsString(String thePath) {
        return new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(thePath), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }

}
