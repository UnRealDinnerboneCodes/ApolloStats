package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.function.Supplier;

public class AverageFillPage implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        List<Match> matches = hostMatchMap.values().stream().flatMap(List::stream).toList();
        Map<String, List<Integer>> fills = new HashMap<>();
        matches.forEach(match -> match.findGameData().ifPresent(game -> Maps.putIfAbsent(fills, match.displayName(), new ArrayList<>()).add(game.fill())));

        record HostFill(String host, int smallest, int largest, int average) implements Supplier<List<String>> {
            @Override
            public List<String> get() {
                return Arrays.asList(host, String.valueOf(smallest), String.valueOf(largest), String.valueOf(average));
            }
        }

        List<HostFill> fillsList = new ArrayList<>();

        for(Map.Entry<String, List<Integer>> stringListEntry : fills.entrySet()) {
            List<Integer> fill = stringListEntry.getValue();
            int total = fill.stream().mapToInt(Integer::intValue).sum();
            int min = fill.stream().mapToInt(Integer::intValue).min().orElse(0);
            int max = fill.stream().mapToInt(Integer::intValue).max().orElse(0);
            int average = total / fill.size();
            fillsList.add(new HostFill(stringListEntry.getKey(), min, max, average));
        }

        wrapper.html(WebUtils.makeHTML("Fills", "", Arrays.asList("Host", "Smallest", "Largest", "Average"), fillsList));
    }



    @Override
    public String getPath() {
        return "fills";
    }
}
