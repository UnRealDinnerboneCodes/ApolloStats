package com.unrealdinnerbone.apollostats.generators.test;

import com.google.common.collect.Lists;
import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.apollostats.Util;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.MathHelper;
import com.unrealdinnerbone.unreallib.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomScenarioGenerator implements IWebPage {
    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap, Function<String, String> query) {
        List<Scenarios.Scenario> scens = getScenList();
        String minS = query.apply("min");
        String maxS = query.apply("max");
        int min = minS == null ? 3 : Integer.parseInt(minS);
        int max = maxS == null ? 9 : Math.min(scens.size(), Integer.parseInt(maxS));
        int maxId = scens.stream().max(Comparator.comparing(Scenarios.Scenario::id)).map(Scenarios.Scenario::id).orElseThrow();
        return generatePage(Util.createId(maxId, min, max));

    }

    public static List<Scenarios.Scenario> getScenList() {
        return Scenarios.getValues(Scenarios.Type.SCENARIO).stream()
                .filter(scen -> scen.id() != -1)
                .toList();
    }


    public static String generatePage(String id) {
        Triplet<Integer, Integer, Integer> s = Util.decodeId(id);
        int scenList = s.a();
        int min = s.b();
        int max = s.c();


        List<String> scens = getScenList().stream().filter(scen -> scen.id() <= scenList).map(Scenarios.Scenario::name).collect(Collectors.toList());
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
        List<String> teams = Scenarios.getValues(Scenarios.Type.TEAM)
                .stream().map(Scenarios.Scenario::name)
                .filter(name -> !name.equals("Love at First Lake"))
                        .toList();
        String teamType = ArrayUtil.getRandomValue(random, teams);
        String copyMessage = teamType + ", " + String.join(", ", randomSelect);
        copyMessage = copyMessage + "(https://apollo.unreal.codes/random_game/" + id + ")";
        String page  = getPage().replace("{STUFF}", copyMessage).replace("{data}", builder.toString()).replace("{team}", Util.formalize(teamType));
        LOGGER.info("Random Scenarios: {} {}", copyMessage, randomSelect);
        return page;
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(RandomScenarioGenerator.class);

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
       return null;
    }

    @Override
    public String getName() {
        return "random_game";
    }


    public static String getPage() {
        return """
                             <meta property="og:url" content="https://apollo.unreal.codes">
                             <meta property="og:title" content="Random Scens">
                             <meta property="og:description" content="The Scens would go here, here, here, here, here, here, here, here, here, here, here">
                             <meta property="og:type" content="website">
                      
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
                <button onclick="copyText()">Click Here to copy scenes to clipboard</button></div>
                      <script>
                            function copyText() {
                                navigator.clipboard.writeText(window.location.href + "/{STUFF}");
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
