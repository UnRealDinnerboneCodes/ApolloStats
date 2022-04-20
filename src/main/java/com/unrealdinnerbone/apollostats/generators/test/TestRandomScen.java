package com.unrealdinnerbone.apollostats.generators.test;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.apollostats.Util;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestRandomScen implements IWebPage {



    private static final Logger LOGGER = LoggerFactory.getLogger(TestRandomScen.class);

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        List<String> random = new ArrayList<>(Scenarios.getValues(Scenarios.Type.SCENARIO));
        random.remove("Rush");
        random.remove("Unknown");
        random.remove("Union Spawn");
        random.remove("Chorus Gapples");
        random.remove("Custom 00");
        random.remove("Farm Gang");
        random.remove("Permanent Invisibility");
        List<String> randomSelect = new ArrayList<>();
        String page = getPage();
        for(int i = 1; i <= 6; i++) {
            String type = ArrayUtil.getRandomValueAndRemove(random);
            page = page.replace("{img_0" + i + "}", Util.formalize(type));
            randomSelect.add(type);
        }
        List<String> teams = new ArrayList<>(Scenarios.getValues(Scenarios.Type.TEAM));
        teams.remove("Love at First Lake");
        page = page.replace("{team}", Util.formalize(ArrayUtil.getRandomValue(teams)));
        LOGGER.info("Random Scenarios: {}", randomSelect);
        return page;
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
                <tr>
                  <td><img src = "/img/scens/{img_01}.png" class="img"></td>
                  <td><img src = "/img/scens/{img_02}.png" class="img"></td>
                  <td><img src = "/img/scens/{img_03}.png" class="img"></td>
                </tr>
                <tr>
                  <td><img src = "/img/scens/{img_04}.png" class="img"></td>
                  <td><img src = "/img/scens/{img_05}.png" class="img"></td>
                  <td><img src = "/img/scens/{img_06}.png" class="img"></td>
                </tr>
              </table>
                       
              <style>
                  .center {
                    margin-left: auto;
                    margin-right: auto;
                  }
            </style>
                """;
    }
}
