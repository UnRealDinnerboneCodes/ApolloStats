package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TeamSizeGames implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        Map<String, Map<Integer, AtomicInteger>> types = new HashMap<>();
        List<String> typesList = new ArrayList<>();


        hostMatchMap.forEach((host, matches) -> {
            Map<Integer, AtomicInteger> map = new HashMap<>();
            for(Match match : matches) {
                if(match.isApolloGame() && !match.removed()) {
                    int size = match.size() == null ? 0 : match.size();
                    Maps.putIfAbsent(map, size, new AtomicInteger(0)).incrementAndGet();
                    if(!typesList.contains(String.format("%03d", size))) {
                        typesList.add(String.format("%03d", size));
                    }

                }
            }
            types.put(host, map);
        });


        List<Supplier<List<String>>> iTableData = new ArrayList<>();

        typesList.sort(Comparator.naturalOrder());
        typesList.add(0, "Host");

        for(Map.Entry<String, Map<Integer, AtomicInteger>> entry : types.entrySet()) {
            iTableData.add(() -> {
                List<String> values = new ArrayList<>();
                values.add(entry.getKey());
                for(String s : typesList) {
                    if(!s.equals("Host")) {
                        values.add(String.valueOf(entry.getValue().getOrDefault(Integer.valueOf(s), new AtomicInteger(0)).get()));
                    }
                }
                return values;
            });
        }
        return WebUtils.makeHTML("Team Types", "",typesList, iTableData);
    }

    @Override
    public String getName() {
        return "team_size";
    }

}
