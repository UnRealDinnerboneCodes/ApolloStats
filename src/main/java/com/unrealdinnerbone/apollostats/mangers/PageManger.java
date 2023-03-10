package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.api.IManger;
import com.unrealdinnerbone.apollostats.api.WebInstance;
import com.unrealdinnerbone.apollostats.instacnes.APIInstance;
import com.unrealdinnerbone.apollostats.instacnes.PublicInstance;
import com.unrealdinnerbone.apollostats.web.WebAccessManger;
import com.unrealdinnerbone.apollostats.web.pages.MainPage;
import com.unrealdinnerbone.apollostats.web.pages.bingo.BingoPages;
import com.unrealdinnerbone.apollostats.web.pages.generator.RandomScenarioGenerator;
import com.unrealdinnerbone.apollostats.web.pages.graph.GameHostedGen;
import com.unrealdinnerbone.apollostats.web.pages.graph.TotalGameGen;
import com.unrealdinnerbone.apollostats.web.pages.stats.*;
import com.unrealdinnerbone.apollostats.web.pages.stats.old.HostIn24HoursGen;
import com.unrealdinnerbone.apollostats.web.pages.stats.old.TeamTypesGames;
import com.unrealdinnerbone.apollostats.web.pages.stats.old.TimeBetweenGames;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import io.javalin.Javalin;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PageManger implements IManger {

    private static final Logger LOGGER = LogHelper.getLogger();
    private final List<WebInstance<?>> instances = new ArrayList<>();

    public PageManger() {
        instances.add(
                new PublicInstance(Arrays.asList(
                        new RandomScenarioGenerator(),
                        new RandomScenarioGenerator.IDPage(),
                        new TeamTypesGames(),
                        new LastPlayedGen(),
                        new AverageFillPage(),
                        new GameHostedGen(),
                        new BingoPages.IDCard(),
                        new BingoPages.NewCard(),
                        new TeamSizeGames(),
                        new HostIn24HoursGen(),
                        new GameHistory(),
                        new FunnyScenNames(),
                        new TotalGameGen(),
                        new MainPage(),
                        new DifferentHostInARow(),
                        new GamesPage(),
                        new NetherGamePage(),
                        new DaysInARowPage(),
                        new TimeBetweenGames()
                )));

        instances.add(
                new APIInstance(Arrays.asList(new BingoPages.Add())
                ));
    }
    @Override
    public CompletableFuture<Void> start() {
        return TaskScheduler.runAsync(() -> {
            for (WebInstance<?> instance : instances) {
                Javalin javalin = Javalin.create(javalinConfig -> {
                    javalinConfig.accessManager(new WebAccessManger());
                    javalinConfig.showJavalinBanner = false;
                    instance.getConfig().accept(javalinConfig);
                }).start(instance.getPort());

                instance.getPages().forEach((key, value) -> value.forEach(iWebPage -> {
                    if (key == WebInstance.Type.GET) {
                        LOGGER.info("Registering GET page {}", iWebPage.getPath());
                        javalin.get(iWebPage.getPath(), iWebPage::getPage, iWebPage.getRole());
                    } else if (key == WebInstance.Type.POST) {
                        LOGGER.info("Registering POST page {}", iWebPage.getPath());
                        javalin.post(iWebPage.getPath(), iWebPage::getPage, iWebPage.getRole());
                    }
                }));
            }
        });
    }

    @Override
    public String getName() {
        return "Page Manager";
    }
}
