package com.unrealdinnerbone.apollostats;

import com.google.common.base.Stopwatch;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.ShutdownUtils;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public interface StatsLauncher {

    Logger LOGGER = LogHelper.getLogger();
    static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOGGER.info("Starting ApolloStats...");
        Stats stats = new Stats();
        Stats.INSTANCE = stats;
        stats.register();
        stats.start().whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to start ApolloStats", throwable.getCause() != null ? throwable.getCause() : throwable);
            } else {
                LOGGER.info("Started ApolloStats in {}s", stopwatch.stop().elapsed(TimeUnit.SECONDS));
            }
        });
        ShutdownUtils.addShutdownHook(() -> LOGGER.info("Stopping ApolloStats"));
    }
}
