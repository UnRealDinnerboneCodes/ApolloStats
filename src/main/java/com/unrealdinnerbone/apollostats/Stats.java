package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.apollostats.api.IManger;
import com.unrealdinnerbone.apollostats.lib.Config;
import com.unrealdinnerbone.apollostats.mangers.*;
import com.unrealdinnerbone.config.ConfigManager;
import com.unrealdinnerbone.postgresslib.PostgresConfig;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Stats {
    private static final Logger LOGGER = LogHelper.getLogger();

    private static final List<IManger> futures = new ArrayList<>();

    public static Stats INSTANCE;

    private final Config statsConfig;
    private final PostgressHandler postgressHandler;
    private final StaffManager staffManager;
    private final BingoManger bingoManger;
    private final ScenarioManager scenarioManager;
    private final GameManager gameManager;

    private final MatchManger matchManger;

    private final PageManger pageManger;

    private final Executor executor = Executors.newFixedThreadPool(5);

    public Stats() throws IllegalStateException {
        ConfigManager configManager = ConfigManager.createSimpleEnvPropertyConfigManger();
        statsConfig = configManager.loadConfig("apollo", Config::new);
        PostgresConfig postgresConfig = configManager.loadConfig("postgres", PostgresConfig::new);
        LOGGER.info("Connecting to database...");
        try {
            postgressHandler = new PostgressHandler(postgresConfig);
        }catch (SQLException e) {
            LOGGER.error("Failed to connect to database", e);
            throw new IllegalStateException(e);
        }
        staffManager = register(new StaffManager());
        bingoManger = register(new BingoManger());
        scenarioManager = register(new ScenarioManager());
        gameManager = register(new GameManager());
        matchManger = new MatchManger();
        pageManger = new PageManger();
    }

    private <T extends IManger> T register(T t) {
        futures.add(t);
        return t;
    }
    public CompletableFuture<Void> start() {
        AlertManager.init();
        return TaskScheduler.allAsync(futures.stream().map(this::map).toList())
                .thenCompose((v) -> map(matchManger))
                .thenCompose((v) -> map(pageManger))
                ;
    }

    private CompletableFuture<Void> map(IManger manger) {
        return manger.start().whenComplete((v, e) -> {
            if (e == null) {
                LOGGER.info("Started {}", manger.getName());
            }
        });
    }


    public PostgressHandler getPostgresHandler() {
        return postgressHandler;
    }

    public Config getStatsConfig() {
        return statsConfig;
    }

    public BingoManger getBingoManger() {
        return bingoManger;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public MatchManger getMatchManger() {
        return matchManger;
    }

    public ScenarioManager getScenarioManager() {
        return scenarioManager;
    }

    public StaffManager getStaffManager() {
        return staffManager;
    }

    public Executor getExecutor() {
        return executor;
    }

    public static String getResourceAsString(String thePath) {
        return new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(thePath), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }


}
