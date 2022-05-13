package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.apollostats.api.BingoValue;
import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.WebInstance;
import com.unrealdinnerbone.apollostats.instacnes.APIInstance;
import com.unrealdinnerbone.apollostats.instacnes.PublicInstance;
import com.unrealdinnerbone.apollostats.lib.Config;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.*;
import com.unrealdinnerbone.apollostats.web.ApolloRole;
import com.unrealdinnerbone.apollostats.web.Results;
import com.unrealdinnerbone.apollostats.web.WebAccessManger;
import com.unrealdinnerbone.apollostats.web.pages.bingo.BingoPages;
import com.unrealdinnerbone.apollostats.web.pages.graph.FillsGraphPage;
import com.unrealdinnerbone.apollostats.web.pages.graph.GameHostedGen;
import com.unrealdinnerbone.apollostats.web.pages.generator.RandomScenarioGenerator;
import com.unrealdinnerbone.apollostats.web.pages.stats.*;
import com.unrealdinnerbone.config.ConfigManager;
import com.unrealdinnerbone.postgresslib.PostgresConfig;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.LazyValue;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.discord.DiscordWebhook;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import io.javalin.Javalin;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Stats {
    private static final Logger LOGGER = LoggerFactory.getLogger("Stats");
    private static final List<WebInstance<?>> instances = new ArrayList<>();
    private static final List<Supplier<CompletableFuture<Void>>> futures = new ArrayList<>();

    public static final Config CONFIG;
    private static final PostgresConfig POSTGRES_CONFIG;

    private static final LazyValue<PostgressHandler> HANDLER;

    static {
        ConfigManager configManager = ConfigManager.createSimpleEnvPropertyConfigManger();
        CONFIG = configManager.loadConfig("apollo", Config::new);
        POSTGRES_CONFIG = configManager.loadConfig("postgres", PostgresConfig::new);
        HANDLER = new LazyValue<>(() -> {
            try {
                return new PostgressHandler(POSTGRES_CONFIG);
            }catch(SQLException | ClassNotFoundException e) {
                LOGGER.error("Failed to create postgres handler", e);
                return null;
            }
        });
        instances.add(
                new PublicInstance(Arrays.asList(
                        new TopScenariosGen(),
                        new LastPlayedGen(),
                        new AverageFillPage(),
                        new GameHostedGen(),
                        new FillsGraphPage(),
                        new BingoPages.IDCard(),
                        new BingoPages.NewCard(),
                        new RandomScenarioGenerator(),
                        new RandomScenarioGenerator.IDPage()
        )));

        instances.add(
                new APIInstance(Arrays.asList(new BingoPages.Add())
        ));

        futures.add(BingoManger::init);
        futures.add(ScenarioManager::init);
        futures.add(StaffManager::load);
        futures.add(GameManager::init);
        futures.add(MatchManger::init);
    }

    public static void main(String[] args) {
        TaskScheduler.allAsync(futures.stream().map(Supplier::get).toList()).whenComplete((v, t) -> {
            if (t != null) {
                LOGGER.error("Failed to load data", t);
            }else {
                for(WebInstance<?> instance : instances) {
                    Javalin javalin = Javalin.create(javalinConfig -> {
                        javalinConfig.accessManager(new WebAccessManger());
                        instance.getConfig().accept(javalinConfig);
                    }).start(instance.getPort());

                    instance.getPages().forEach((key, value) -> value.forEach(iWebPage -> {
                        if(key == WebInstance.Type.GET) {
                            javalin.get(iWebPage.getPath(), iWebPage::getPage, iWebPage.getRole());
                        }else if(key == WebInstance.Type.POST) {
                            javalin.post(iWebPage.getPath(), iWebPage::getPage, iWebPage.getRole());
                        }
                    }));
                }
            }
        });

    }


    public static PostgressHandler getPostgresHandler() {
        return HANDLER.get();
    }






    public static String getResourceAsString(String thePath) {
        return new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(thePath), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }


}
