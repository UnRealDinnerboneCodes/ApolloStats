package com.unrealdinnerbone.apollo.core;

import com.unrealdinnerbone.apollo.core.api.IManger;
import com.unrealdinnerbone.apollo.core.api.event.ConfigRegisterEvent;
import com.unrealdinnerbone.apollo.core.lib.GeneralConfig;
import com.unrealdinnerbone.apollo.core.mangers.GameManager;
import com.unrealdinnerbone.apollo.core.mangers.MatchManger;
import com.unrealdinnerbone.apollo.core.mangers.ScenarioManager;
import com.unrealdinnerbone.apollo.core.mangers.StaffManager;
import com.unrealdinnerbone.config.impl.provider.EnvProvider;
import com.unrealdinnerbone.postgresslib.PostgresConfig;
import com.unrealdinnerbone.postgresslib.PostgresHandler;
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


    public static Stats INSTANCE;

    private final StaffManager staffManager = new StaffManager();
    private final ScenarioManager scenarioManager = new ScenarioManager();
    private final GameManager gameManager = new GameManager();
    private final MatchManger matchManger = new MatchManger();


    private final Executor executor = Executors.newFixedThreadPool(5);

    private PostgresHandler postgressHandler;

    private PostgresConfig postgresConfig;

    private GeneralConfig generalConfig;



    public Stats() throws IllegalStateException {
        ApolloEventManager.EVENT_MANAGER.registerHandler(ConfigRegisterEvent.class, configRegisterEvent -> registerConfigs(configRegisterEvent.envProvider()));
    }

    public void registerConfigs(EnvProvider envProvider) {
        postgresConfig = envProvider.loadConfig("postgres", PostgresConfig::new);
        generalConfig = envProvider.loadConfig("general", GeneralConfig::new);
    }



    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                postgressHandler = new PostgresHandler(postgresConfig);
            } catch (SQLException e) {
                LOGGER.error("Failed to connect to Postgres", e);
                throw new IllegalStateException("Failed to connect to Postgres", e);
            }
        }, executor).thenCompose(aVoid -> staffManager.start()
                .thenCompose(aVoid1 -> scenarioManager.start()
                        .thenCompose(aVoid2 -> gameManager.start()
                                .thenCompose(aVoid3 -> matchManger.start()))));
    }

    private CompletableFuture<Void> map(IManger manger) {
        return manger.start().whenComplete((v, e) -> {
            if (e == null) {
                LOGGER.info("Started {}", manger.getName());
            }
        });
    }


    public PostgresHandler getPostgresHandler() {
        return postgressHandler;
    }

    public GeneralConfig getStatsConfig() {
        return generalConfig;
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
