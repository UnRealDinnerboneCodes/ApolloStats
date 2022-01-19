package com.unrealdinnerbone.apollostats.generators;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamTypesGames implements IWebPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamTypesGames.class);

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        Map<String, Map<String, AtomicInteger>> types = new HashMap<>();
        List<String> typesList = new ArrayList<>();
        typesList.add("Host");


        hostMatchMap.forEach((host, matches) -> {
            Map<String, AtomicInteger> map = new HashMap<>();
            for(Match match : matches) {
                if(match.isApolloGame() && !match.removed()) {
                    Maps.putIfAbsent(map, match.teams(), new AtomicInteger(0)).incrementAndGet();
                    if(!typesList.contains(match.teams())) {
                        typesList.add(match.teams());
                    }

                }
            }
            types.put(host, map);
        });


        List<WebUtils.ITableData> iTableData = new ArrayList<>();

        for(Map.Entry<String, Map<String, AtomicInteger>> entry : types.entrySet()) {
            iTableData.add(() -> {
                List<String> values = new ArrayList<>();
                values.add(entry.getKey());
                for(String s : typesList) {
                    if(!s.equals("Host")) {
                        values.add(String.valueOf(entry.getValue().getOrDefault(s, new AtomicInteger(0)).get()));
                    }
                }
                return values;
            });
        }
        return WebUtils.makeHTML("Team Types", typesList, iTableData);
    }

    @Override
    public String getName() {
        return "team_types";
    }

    public record Stats(String name, Pair<Instant, String> first, Pair<Instant, String> last) implements WebUtils.ITableData {

        @Override
        public List<String> getData() {
            return Arrays.asList(name, first.key().toString(), last.key().toString(), first.value(), last.value());
        }
    }
}
