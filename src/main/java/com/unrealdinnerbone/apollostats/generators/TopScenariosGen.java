package com.unrealdinnerbone.apollostats.generators;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Map.Entry;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TopScenariosGen implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        AtomicInteger total = new AtomicInteger(0);
        HashMap<String, AtomicInteger> matchCount = new HashMap<>();

        hostMatchMap.values()
                .forEach(value -> value.stream()
                        .filter(Match::isApolloGame)
                        .filter(Predicate.not(Match::removed))
                        .peek(match -> total.incrementAndGet())
                        .forEach(match -> Scenarios.fixScenarios(match.scenarios())
                                .forEach(scenario -> Maps.putIfAbsent(matchCount, scenario, new AtomicInteger(0)).incrementAndGet())));


        List<Count> stats = new ArrayList<>();
        matchCount.forEach((key, times) -> {
            double percent = times.get() / (double)total.get();
            stats.add(new Count(key, times.get(), percent * 100));
        });


        stats.sort(Comparator.comparing(Count::percent).reversed());
        return WebUtils.makeHTML("Top Scenarios", Arrays.asList("Scenario", "Count", "Percent"), stats);
    }

    @Override
    public String getName() {
        return "top";
    }


    public record Count(String scenario, int amount, double percent) implements WebUtils.ITableData {

        private final static DecimalFormat df = new DecimalFormat("#.#####");

        @Override
        public List<String> getData() {
            return Arrays.asList(scenario, String.valueOf(amount), df.format(percent) + "%");
        }
    }

}
