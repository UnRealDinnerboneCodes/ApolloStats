package com.unrealdinnerbone.apollostats.mangers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomScenManger {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomScenManger.class);
    private static final Cache<String, String> CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    public static String getPage(String id, int amount) throws Exception {
        return CACHE.get(id, () -> findCardData(id)
                .orElseGet(() -> createCard(id, amount))
                .toHtml(false));
    }

    private static RandomData createCard(String id, int amount) throws RuntimeException {
        List<Scenario> scenarios = getList(Type.SCENARIO)
                .stream()
                .sorted(ArrayUtil.shuffle())
                .limit(amount)
                .toList();

        Scenario team = getList(Type.TEAM)
                .stream()
                .min(ArrayUtil.shuffle())
                .orElseThrow(() -> new RuntimeException("Could not find a team scenario"));

        TaskScheduler.handleTaskOnThread(() -> {
            try {
                Stats.getPostgresHandler().executeUpdate("INSERT INTO public.random_scen (scens, id, team) VALUES (?, ?, ?)", statement -> {
                    statement.setString(1, scenarios.stream().map(Scenario::id).map(String::valueOf).collect(Collectors.joining(";")));
                    statement.setString(2, id);
                    statement.setInt(3, team.id());
                });
            }catch(Exception e) {
                LOGGER.error("Error while inserting random scen data", e);
            }
        });
        return new RandomData(scenarios, id, team);

    }

    private static List<Scenario> getList(Type type) {
        return ScenarioManager.getValues(type)
                .stream()
                .filter(Scenario::official)
                .filter(Scenario::image)
                .toList();
    }

    public static Optional<String> find(String id) throws SQLException, RuntimeException {
        return findCardData(id).map(randomData -> randomData.toHtml(true));
    }

    private static Optional<RandomData> findCardData(String id) throws SQLException, RuntimeException {
        ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.random_scen where id = ?", statement -> statement.setString(1, id));
        if(resultSet.next()) {
            String values = resultSet.getString("scens");
            int team = resultSet.getInt("team");
            Scenario teamScen = ScenarioManager.find(team).orElseThrow(() -> new RuntimeException("Can't find Team scenario with id " + team));
            List<Scenario> scenarios = Arrays.stream(values.split(";"))
                    .map(Integer::parseInt)
                    .map(ScenarioManager::find)
                    .map(scenario -> scenario.orElseThrow(() -> new RuntimeException("Can't find scenario with id " + scenario)))
                    .toList();
            return Optional.of(new RandomData(scenarios, id, teamScen));
        }else {
            return Optional.empty();
        }
    }

    public record RandomData(List<Scenario> scenarios, String id, Scenario team) {
        public String toHtml(boolean embed) {
            StringBuilder builder = new StringBuilder();
            for(List<Scenario> strings : Lists.partition(scenarios, 3)) {
                String row = getLine();
                int used = 0;
                for(int i = 0; i < strings.size(); i++) {
                    row = row.replace("{img_0" + (i + 1) + "}", Util.formalize(strings.get(i).name()));
                    used = i;
                }
                if(used < 3) {
                    for(int i = used; i <= 3; i++) {
                        String toReplace = "<img src = \"/img/scens/{img_0x}.png\" class=\"img\">".replace("x", i + "");
                        row = row.replace(toReplace, "");
                    }
                }
                builder.append(row);
            }

            String scens = scenarios.stream().map(Scenario::name).collect(Collectors.joining(", "));
            String copyMessage = team.name() + ", " + scens;
            String page  = getPage().replace("{STUFF}", copyMessage)
                    .replace("{ID}", embed ? "" : "/" + id)
                    .replace("{data}", builder.toString())
                    .replace("{EMBED}", embed ? createDiscordEmbed(copyMessage) : "")
                    .replace("{team}", Util.formalize(team.name()));
            LOGGER.info("Random Scenarios: {} {}", copyMessage, scens);
            return page;
        }




        public static String createDiscordEmbed(String content) {
            return
                    """
                    <meta property="og:description" content="{}">""".replace("{}", content);
        }


        public static String getPage() {
            return """
                             {EMBED}
                             
                      
                             <link rel="stylesheet" type="text/css" href="../css/stats.css">
                <table class="center">
                    <tr>
                        <td></td>
                        <td><img src = "/img/teams/{team}.png" class="img"></td>
                        <td></td>
                    </tr>
                    {data}
                  </table>

                  <style>
                      .center {
                        margin-left: auto;
                        margin-right: auto;
                      }
                      .center_table {
                          display: flex;
                          justify-content: center;
                          align-items: center;
                      }
                </style>
                <div class="center_table">
                <button onclick="copyTextTwo()">Copy Discord Share Link</button>
                <br></br>
                <button onclick="copyText()">Copy Scenarios to text</button>
                </div>
                      <script>
                            function copyText() {
                                navigator.clipboard.writeText("{STUFF}");
                            }
                            function copyTextTwo() {
                                navigator.clipboard.writeText(window.location.href + "{ID}");
                            }
                        </script>

                    """;
        }

        public static String getLine() {
            return """
                   <tr>
                  <td><img src = "/img/scens/{img_01}.png" class="img"></td>
                  <td><img src = "/img/scens/{img_02}.png" class="img"></td>
                  <td><img src = "/img/scens/{img_03}.png" class="img"></td>
                </tr>
                """;
        }
    }

}
