package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AverageFillPage implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, List<Integer>> fills = hostMatchMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, staffListEntry -> staffListEntry.getValue().stream()
                        .map(Match::findGameData)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(Game::fill)
                        .toList(), (a, b) -> b));


        List<HostFill> fillsList = new ArrayList<>();

        fills.forEach((staff, fill) -> {
            if(fill.size() != 0) {
                int total = fill.stream().mapToInt(Integer::intValue).sum();
                int min = fill.stream().mapToInt(Integer::intValue).min().orElse(0);
                int max = fill.stream().mapToInt(Integer::intValue).max().orElse(0);
                int average = total / fill.size();
                fillsList.add(new HostFill(staff.displayName(), min, max, average, fill.size()));
            }
        });

        wrapper.html(WebUtils.makeHTML("Fills", "", Arrays.asList("Host", "Smallest", "Largest", "Average", "Games"), fillsList));
    }


    record HostFill(String host, int smallest, int largest, int average, int gamesPlayed) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            return Arrays.asList(host, String.valueOf(smallest), String.valueOf(largest), String.valueOf(average), String.valueOf(gamesPlayed));
        }
    }


    @Override
    public String getPath() {
        return "fills";
    }
}
