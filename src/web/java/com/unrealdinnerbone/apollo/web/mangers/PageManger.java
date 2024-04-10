package com.unrealdinnerbone.apollo.web.mangers;

import com.google.common.base.Stopwatch;
import com.unrealdinnerbone.apollo.core.api.IManger;
import com.unrealdinnerbone.apollo.web.api.WebInstance;
import com.unrealdinnerbone.apollo.web.instacnes.PublicInstance;
import com.unrealdinnerbone.apollo.web.pages.MainPage;
import com.unrealdinnerbone.apollo.web.pages.generator.RandomScenarioGenerator;
import com.unrealdinnerbone.apollo.web.pages.graph.GameHostedGen;
import com.unrealdinnerbone.apollo.web.pages.graph.TotalGameGen;
import com.unrealdinnerbone.apollo.web.pages.stats.CalendarPage;
import com.unrealdinnerbone.apollo.web.pages.stats.DifferentHostInARow;
import com.unrealdinnerbone.apollo.web.pages.stats.FunnyScenNames;
import com.unrealdinnerbone.apollo.web.pages.stats.GameFinderPage;
import com.unrealdinnerbone.apollo.web.pages.stats.GameHistory;
import com.unrealdinnerbone.apollo.web.pages.stats.GamePage;
import com.unrealdinnerbone.apollo.web.pages.stats.LastPlayedGen;
import com.unrealdinnerbone.apollo.web.pages.stats.MatchesPage;
import com.unrealdinnerbone.apollo.web.pages.stats.TeamSizeGames;
import com.unrealdinnerbone.apollo.web.pages.stats.TeamsPage;
import com.unrealdinnerbone.apollo.web.pages.stats.TheEndPage;
import com.unrealdinnerbone.apollo.web.pages.stats.hosts.HostsPage;
import com.unrealdinnerbone.apollo.web.pages.stats.old.TeamTypesGames;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import io.javalin.Javalin;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
                        new GameHostedGen(),
                        new TeamSizeGames(),
                        new GameHistory(),
                        new FunnyScenNames(),
                        new TotalGameGen(),
                        new MainPage(),
                        new DifferentHostInARow(),
                        new MatchesPage(),
                        new GameFinderPage(),
                        new TeamsPage(),
                        new HostsPage(),
                        new CalendarPage(),
                        new GamePage(),
                        new TheEndPage()
                )));
    }


    @Override
    public CompletableFuture<Void> start() {
        LOGGER.info("Starting Page Manager");
        return TaskScheduler.runAsync(() -> {
            for (WebInstance<?> instance : instances) {
                Javalin javalin = Javalin.create(javalinConfig -> {
                    javalinConfig.showJavalinBanner = false;
                    instance.getConfig().accept(javalinConfig);
                }).start(instance.getPort());



                instance.getPages().forEach((key, value) -> value.forEach(iWebPage -> {
                    if (key == WebInstance.Type.GET) {
                        LOGGER.info("Registering GET page {}", iWebPage.getPath());
                        javalin.get(iWebPage.getPath(), ctx -> {
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            iWebPage.getPage(ctx);
                            String header = ctx.header("X-Forwarded-For");
                            LOGGER.info("[{}] Took {} to get page {}", header ==null ? "" : Objects.hash(header), stopwatch.stop(), iWebPage.getPath() + ctx.queryString());
                        });
                    } else if (key == WebInstance.Type.POST) {
                        LOGGER.info("Registering POST page {}", iWebPage.getPath());
                        javalin.post(iWebPage.getPath(), iWebPage::getPage);
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
