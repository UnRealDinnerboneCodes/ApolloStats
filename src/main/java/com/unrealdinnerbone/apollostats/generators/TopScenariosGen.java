package com.unrealdinnerbone.apollostats.generators;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.Map.Entry;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TopScenariosGen implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        HashMap<String, AtomicInteger> matchCount = new HashMap<>();

        hostMatchMap.values()
                .forEach(value -> value.stream()
                        .filter(Match::isApolloGame)
                        .filter(Predicate.not(Match::removed))
                        .forEach(match -> Scenarios.fixScenarios(match.scenarios())
                                .forEach(scenario -> Maps.putIfAbsent(matchCount, scenario, new AtomicInteger(0)).incrementAndGet())));

        List<Count> counts = new ArrayList<>();
        sortByComparator(matchCount, false).forEach((key, value) -> counts.add(new Count(key, value)));
        return WebUtils.makeHTML("Top Scenarios", Arrays.asList("Scenario", "Count"), counts);
    }

    @Override
    public String getName() {
        return "top";
    }


    public record Count(String scenario, int amount) implements WebUtils.ITableData {

        @Override
        public List<String> getData() {
            return Arrays.asList(scenario, String.valueOf(amount));
        }
    }


    public static Map<String, Integer> sortByComparator(Map<String, AtomicInteger> unsortMap, final boolean order)
    {
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        unsortMap.forEach((key, value) -> sortedMap.put(key, value.get()));
        List<Entry<String, Integer>> list = new LinkedList<>(sortedMap.entrySet());
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }
}
