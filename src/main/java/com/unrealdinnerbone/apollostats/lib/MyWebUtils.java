package com.unrealdinnerbone.apollostats.lib;

import com.unrealdinnerbone.unreallib.Pair;

import java.util.List;
import java.util.Map;

public class MyWebUtils {

    public static String makeCardPage(String title, String icon, String key, Map<String, String> filtersMap, List<Pair<String, List<Pair<String, String>>>> stringMapMap) {
        StringBuilder links = new StringBuilder();
        filtersMap.forEach((link, name) -> links.append(createLink(name, link)));
        StringBuilder cards = new StringBuilder();
        stringMapMap.forEach((pair) -> cards.append(createCard(pair.key(), key, pair.value())));
        return """
                <!DOCTYPE html>
                <html>
                <title>{title}</title>
                <link rel="shortcut icon" href="{ICON}">
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link rel="stylesheet" type="text/css" href="css/cards.css">
                </head>
                <body>

                <!-- MAIN (Center website) -->
                <div class="main">

                    <div class="dropdown">
                        <button onclick="myFunction()" class="dropbtn">Sort</button>
                        <div id="myDropdown" class="dropdown-content">
                            <input type="text" placeholder="Filter.." id="myInput" onkeyup="filterFunction()">
                            {LINKS}
                        </div>
                    </div>

                  

                </div>
                  <div class="row">
                        {CARDS}
                    </div>
                                <script src="js/cards.js" type="text/javascript"></script>

                </body>
                </html>""".replace("{title}", title)
                .replace("{ICON}", icon)
                .replace("{LINKS}", links.toString())
                .replace("{CARDS}", cards.toString());
    }

    private static String createLink(String link, String name) {
        return "<a href=\"{LINK}\">{NAME}</a>".replace("{LINK}", link).replace("{NAME}", name);
    }

    private static String createCard(String name, String key, List<Pair<String, String>> stats) {
        StringBuilder stringBuilder = new StringBuilder();
        stats.forEach((stat) -> stringBuilder.append(createStats(stat.key(), stat.value())));
        return """
                            <div class="content">
                            <h4>{KEY}</h4>
                                <h4>{NAME}</h4>
                                {STATS}
                            </div>
                """.replace("{NAME}", name).replace("{STATS}", stringBuilder.toString())
                .replace("{KEY}", key);
    }

    private static String createStats(String stats, String value) {
        return """
               <div class="stats">
               <p>{STAT}</p>
               <p>{VALUE}</p>
               </div>
                """.replace("{STAT}", stats).replace("{VALUE}", value);
    }
}
