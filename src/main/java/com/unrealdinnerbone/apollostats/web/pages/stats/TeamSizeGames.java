package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class TeamSizeGames implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, Map<Integer, AtomicInteger>> types = new HashMap<>();
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

        for(Map.Entry<Staff, Map<Integer, AtomicInteger>> entry : types.entrySet()) {
            iTableData.add(() -> {
                List<String> values = new ArrayList<>();
                values.add(entry.getKey().displayName());
                for(String s : typesList) {
                    if(!s.equals("Host")) {
                        values.add(String.valueOf(entry.getValue().getOrDefault(Integer.valueOf(s), new AtomicInteger(0)).get()));
                    }
                }
                return values;
            });
        }
        wrapper.html(WebUtils.makeHTML("Team Types", "",typesList, iTableData));
    }

    @Override
    public String getPath() {
        return "team_size";
    }

}
