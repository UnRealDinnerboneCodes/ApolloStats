package com.unrealdinnerbone.apollostats;

import com.google.common.base.Stopwatch;
import com.unrealdinnerbone.apollo.core.ApolloEventManager;
import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.event.ConfigRegisterEvent;
import com.unrealdinnerbone.apollo.core.api.module.IModule;
import com.unrealdinnerbone.apollo.core.api.module.Module;
import com.unrealdinnerbone.config.api.ConfigCreator;
import com.unrealdinnerbone.config.api.exception.ConfigException;
import com.unrealdinnerbone.config.config.ConfigValue;
import com.unrealdinnerbone.config.impl.provider.EnvProvider;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.ShutdownUtils;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface StatsLauncher {

    Logger LOGGER = LogHelper.getLogger();

    static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOGGER.info("Starting ApolloStats...");

        EnvProvider envProvider = new EnvProvider();
        LaunchConfig launchConfig = envProvider.loadConfig("start", LaunchConfig::new);
        try {
            envProvider.read();
        } catch (ConfigException e) {
            LOGGER.error("Failed to read Config", e);
            ShutdownUtils.shutdown();
        }
        List<IModule> modules = new ArrayList<>();
        Reflections reflections = new Reflections("com.unrealdinnerbone.apollo", StatsLauncher.class);
        for (Class<?> aClass : reflections.getTypesAnnotatedWith(Module.class)) {
            Module moduleData = aClass.getAnnotation(Module.class);
            if(launchConfig.disabledModules.get().contains(moduleData.value())) {
                LOGGER.info("Skipping Module: {}", moduleData.value());
                continue;
            }
            LOGGER.info("Creating Module: {}", moduleData.value());

            try {
                IModule iModule = (IModule) aClass.getConstructor().newInstance();
                iModule.registerEventHandlers(ApolloEventManager.EVENT_MANAGER);
                modules.add(iModule);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                LOGGER.error("Failed to create Module: {}", moduleData.value(), e);
            }
        }

        Stats stats = new Stats();
        Stats.INSTANCE = stats;
        ApolloEventManager.EVENT_MANAGER.post(new ConfigRegisterEvent(envProvider));
        try {
            envProvider.read();
        } catch (ConfigException e) {
            LOGGER.error("Failed to read Config", e);
            ShutdownUtils.shutdown();
        }
        try {
            stats.start();
        } catch (SQLException e) {
            LOGGER.error("Failed to start Stats", e);
            ShutdownUtils.shutdown();
        }


        for (IModule module : modules) {
            Stopwatch moduleStopwatch = Stopwatch.createStarted();
            LOGGER.info("Starting Module: {}", module.getClass().getSimpleName());
            try {
                module.start();
            }catch (Exception e) {
                LOGGER.error("Failed to start Module: {}", module.getClass().getSimpleName(), e);
                ShutdownUtils.shutdown();
            }
            LOGGER.info("Started Module: {} in {}s", module.getClass().getSimpleName(), moduleStopwatch.stop().elapsed(TimeUnit.SECONDS));
        }

        ShutdownUtils.addShutdownHook(() -> LOGGER.info("Stopping ApolloStats"));
    }


    public static class LaunchConfig {

        private final ConfigValue<List<String>> disabledModules;

        public LaunchConfig(ConfigCreator configCreator) {
            disabledModules = configCreator.createList("disabledModules", new ArrayList<>(), String.class);
        }
    }
}
