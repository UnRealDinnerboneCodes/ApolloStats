package com.unrealdinnerbone.apollostats;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unrealdinnerbone.apollostats.generators.*;
import com.unrealdinnerbone.apollostats.generators.test.TestRandomScen;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import com.unrealdinnerbone.unreallib.web.HttpUtils;
import io.javalin.Javalin;
import io.javalin.core.util.RouteOverviewPlugin;
import io.javalin.http.ContentType;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Stats {
    private static final Logger LOGGER = LoggerFactory.getLogger("Stats");

    private static final List<IWebPage> generators = new ArrayList<>();
    private static final Cache<String, String> pages = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    static {
//        generators.add(new GameHostedGen());
//        generators.add(new TotalGameGen());
        generators.add(new TopScenariosGen());
        generators.add(new LastPlayedGen());
        generators.add(new LongTimeGames());
        generators.add(new TeamTypesGames());
        generators.add(new TeamSizeGames());
        generators.add(new TimeBetweenGames());
        generators.add(new HostIn24HoursGen());
        generators.add(new TestRandomScen());
    }




    public static void main(String[] args) throws Exception {
        Map<String, List<Match>> hostMatchMap = new HashMap<>();
        TaskScheduler.scheduleRepeatingTask(1, TimeUnit.HOURS, new TimerTask() {
            @Override
            public void run() {
                try {
                    Scenarios.loadDiskData();
                    hostMatchMap.clear();
//                    for(String staff : Util.STAFF) {
//                        List<Match> matches = getAllMatchesForHost(staff, Optional.empty());
//                        LOGGER.info("{} has {} matches", staff, matches.stream().filter(Match::isApolloGame).filter(Predicate.not(Match::removed)).count());
//                        hostMatchMap.put(staff, matches);
//                    }
                }catch(Exception e) {
                    LOGGER.error("Error while updating data", e);
                }
            }});

        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.addStaticFiles(staticFileConfig -> {
                staticFileConfig.directory = "img/scens";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.hostedPath = "/img/scens";
            });
            javalinConfig.addStaticFiles(staticFileConfig -> {
                staticFileConfig.directory = "img/teams";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.hostedPath = "/img/teams";
            });
        }).start(1000);

        app.before(ctx -> {

        });

        app.get("css/stats.css", ctx -> ctx.result(getResourceAsString("css/stats.css")));
        app.get("js/fixed.js", ctx -> ctx.result(getResourceAsString("js/fixed.js")));
        app.get("js/sorttable.txt", ctx -> ctx.result(getResourceAsString("js/sorttable.js")));
//        app.get("img/scen/{scen}", ctx -> {
//            String scen = ctx.pathParam("scen");
//            ctx.contentType(ContentType.IMAGE_PNG).result(getResourceAsString("img/scens/" + scen + ".png"));
//        });

        for(IWebPage generator : generators) {
            app.get(generator.getName(), ctx -> {
                LOGGER.info("Received request for {}", generator.getName());
                ctx.html(pages.get(generator.getName(), () -> generator.generateStats(hostMatchMap, ctx::queryParam)));
            });
        }

        app.get("random_game/{id}", ctx -> {
            ctx.html(TestRandomScen.generatePage(UUID.fromString(ctx.pathParam("id")), TestRandomScen.getScenList(), ctx.queryParam("min") == null ? 3 : Integer.parseInt(ctx.queryParam("min")), ctx.queryParam("max") == null ? 9 : Integer.parseInt(ctx.queryParam("max"))));
        });
    }

    public static List<Match> getAllMatchesForHost(String name, Optional<Integer> before) throws Exception {
        String json = HttpUtils.get("https://hosts.uhc.gg/api/hosts/" + name.replace(" ", "%20/") + "/matches?count=50" + before.map(i -> "&before=" + i).orElse("")).body();
        try {
            List<Match> matches = Arrays.stream(Util.parser().parse(Match[].class, json)).collect(Collectors.toList());
            if (matches.size() == 50) {
                matches.addAll(getAllMatchesForHost(name, Optional.of(matches.get(49).id())));
            }
            return matches;
        }catch(AssertionError e) {
            LOGGER.error("Error while parsing json", e);
            return new ArrayList<>();
        }
//        return new ArrayList<>();
    }

    public static String getResourceAsString(String thePath) {
        return new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(thePath), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
    }

}
