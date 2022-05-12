package com.unrealdinnerbone.apollostats.web.pages.generator;

import com.google.common.collect.Lists;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.ScenarioManager;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.MathHelper;
import com.unrealdinnerbone.unreallib.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomScenarioGenerator implements IStatPage {
    @Override
    public String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        List<Scenario> scens = getScenList();
        String minS = wrapper.queryParam("min");
        String maxS = wrapper.queryParam("max");
        int min = minS == null ? 3 : Integer.parseInt(minS);
        int max = maxS == null ? 9 : Math.min(scens.size(), Integer.parseInt(maxS));
        int maxId = scens.stream().max(Comparator.comparing(Scenario::id)).map(Scenario::id).orElseThrow();
        return generatePage(Util.createId(maxId, min, max), false);

    }

    public static List<Scenario> getScenList() {
        return ScenarioManager.getValues(Type.SCENARIO).stream()
                .filter(Scenario::official)
                .filter(Scenario::image)
                .toList();
    }


    public static String generatePage(String id, boolean createEmbed) {
        Triplet<Integer, Integer, Integer> s = Util.decodeId(id);
        int scenList = s.a();
        int min = s.b();
        int max = s.c();


        List<String> scens = getScenList().stream().filter(scen -> scen.id() <= scenList).map(Scenario::name).collect(Collectors.toList());
        Random random = new Random(id.hashCode());

        List<String> randomSelect = IntStream.rangeClosed(0, MathHelper.randomInt(random, min - 1, max + 1))
                .mapToObj(i -> ArrayUtil.getRandomValueAndRemove(random, scens))
                .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();
        for(List<String> strings : Lists.partition(randomSelect, 3)) {
            String row = getLine();
            int used = 0;
            for(int i = 0; i < strings.size(); i++) {
                row = row.replace("{img_0" + (i + 1) + "}", Util.formalize(strings.get(i)));
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
        List<String> teams = ScenarioManager.getValues(Type.TEAM)
                .stream().map(Scenario::name)
                .filter(name -> !name.equals("Love at First Lake"))
                        .toList();
        String teamType = ArrayUtil.getRandomValue(random, teams);
        String copyMessage = teamType + ", " + String.join(", ", randomSelect);
        String page  = getPage().replace("{STUFF}", copyMessage)
                .replace("{ID}", createEmbed ? "" : "/" + id)
                .replace("{data}", builder.toString())
                .replace("{EMBED}", createEmbed ? createDiscordEmbed(copyMessage) : "")
                .replace("{team}", Util.formalize(teamType));
        LOGGER.info("Random Scenarios: {} {}", copyMessage, randomSelect);
        return page;
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(RandomScenarioGenerator.class);

    @Override
    public String getPath() {
        return "random_game";
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

    public static class IDPage implements IStatPage {

        @Override
        public String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
            return RandomScenarioGenerator.generatePage(wrapper.pathParam("id"), true);
        }

        @Override
        public String getPath() {
            return "random_game/{id}";
        }
    }

}
