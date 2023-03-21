package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.stats.types.LastPlayedStat;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.list.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LastPlayedGen implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Scenario, List<Pair<Instant, String>>> plays = new HashMap<>();
        AtomicInteger totalGames = new AtomicInteger();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .peek(match -> totalGames.incrementAndGet())
                .forEach(match -> com.unrealdinnerbone.apollostats.Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios())
                        .stream()
                        .filter(Scenario::official)
                        .toList()
                        .forEach(scenario -> Maps.putIfAbsent(plays, scenario, new ArrayList<>()).add(Pair.of(Instant.parse(match.opens()), match.displayName()))));

        List<LastPlayedStat> stats = new ArrayList<>();

//        List<Pair<Instant, String>> totalGamesPlayed = plays.values().stream().flatMap(List::stream).sorted(Comparator.comparing(Pair::key)).toList();
//        stats.add(new Stats("Total Games", totalGamesPlayed.get(0), totalGamesPlayed.get(totalGamesPlayed.size() - 1), totalGamesPlayed.size(), 1));

        //total games played

        for(Scenario value : com.unrealdinnerbone.apollostats.Stats.INSTANCE.getScenarioManager().getValues(Type.SCENARIO)) {
            if(value.official()) {
                if(!plays.containsKey(value)) {
                    plays.put(value, new ArrayList<>());
                }
            }
        }

        plays.forEach((key, times) -> {
            if(times.size() == 0) {
                stats.add(new LastPlayedStat(key.name(), Pair.of(Instant.ofEpochMilli(0), "None"), Pair.of(Instant.ofEpochMilli(0), "None"), 0, 0));
            }else {
                times.sort(Comparator.comparing(Pair::key));
                double percent = times.size() / (double) totalGames.get();
                stats.add(new LastPlayedStat(key.name(), times.get(0), times.get(times.size() - 1), times.size(), percent));
            }
        });

        stats.sort(Comparator.comparing(stats1 -> stats1.last().key()));
        wrapper.html(WebUtils.makeHtmlTable("Scenarios First / Last Played (Total Games: "+ totalGames.get() + ")", "", Arrays.asList("Scenario", "First Time", "Last Time", "First Host", "Last Host", "Days Since", "Times Hosted", "Percent Hosted"), stats));
    }

    @Override
    public String getPath() {
        return "played";
    }
}
