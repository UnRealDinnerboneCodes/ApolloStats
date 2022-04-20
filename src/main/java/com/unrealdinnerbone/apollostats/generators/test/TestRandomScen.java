package com.unrealdinnerbone.apollostats.generators.test;

import com.google.common.collect.Lists;
import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.apollostats.Util;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestRandomScen implements IWebPage {


    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap, Function<String, String> query) {
        List<String> random = new ArrayList<>(Scenarios.getValues(Scenarios.Type.SCENARIO));
        random.remove("Rush");
        random.remove("Unknown");
        random.remove("Union Spawn");
        random.remove("Chorus Gapples");
        random.remove("Custom 00");
        random.remove("Farm Gang");
        random.remove("Permanent Invisibility");
        List<String> randomSelect = new ArrayList<>();
//        String page = getPage();
        String minS = query.apply("min");
        String maxS = query.apply("max");
        int min = minS == null ? 3 : Integer.parseInt(minS);
        int max = maxS == null ? 9 : Math.min(random.size(), Integer.parseInt(maxS));
        for(int i = 0; i <= MathHelper.randomInt(min -1, max + 1); i++) {
            String type = ArrayUtil.getRandomValueAndRemove(random);
            randomSelect.add(type);
        }
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
        List<String> teams = new ArrayList<>(Scenarios.getValues(Scenarios.Type.TEAM));
        teams.remove("Love at First Lake");
        String teamType = ArrayUtil.getRandomValue(teams);
        String scens = teamType + ", " + randomSelect.stream().collect(Collectors.joining(", "));
        String page  = getPage().replace("{STUFF}", scens).replace("{data}", builder.toString()).replace("{team}", Util.formalize(teamType));
        LOGGER.info("Random Scenarios: {}", randomSelect);
        return page;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRandomScen.class);

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
                             <link rel="stylesheet" type="text/css" href="css/stats.css">
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
                                navigator.clipboard.writeText("{STUFF}");
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
