package com.unrealdinnerbone.apollo.core;

import com.unrealdinnerbone.apollo.core.api.event.ConfigRegisterEvent;
import com.unrealdinnerbone.apollo.core.lib.GeneralConfig;
import com.unrealdinnerbone.apollo.core.mangers.GameManager;
import com.unrealdinnerbone.apollo.core.mangers.MatchManger;
import com.unrealdinnerbone.apollo.core.mangers.ScenarioManager;
import com.unrealdinnerbone.apollo.core.mangers.StaffManager;
import com.unrealdinnerbone.config.impl.provider.EnvProvider;
import com.unrealdinnerbone.postgresslib.PostgresConfig;
import com.unrealdinnerbone.postgresslib.PostgresHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class Stats {

    public static Stats INSTANCE;

    private final StaffManager staffManager = new StaffManager();
    private final ScenarioManager scenarioManager = new ScenarioManager();
    private final GameManager gameManager = new GameManager();
    private final MatchManger matchManger = new MatchManger();

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



    public void start() throws SQLException {
        postgressHandler = new PostgresHandler(postgresConfig);
        staffManager.start();
        scenarioManager.start();
        gameManager.start();
        matchManger.start();
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

    public static String getResourceAsString(String thePath) {
        return new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(thePath), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }


}
