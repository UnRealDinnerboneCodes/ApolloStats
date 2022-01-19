package com.unrealdinnerbone.apollostats.generators;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class PresentUsed implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        AtomicInteger total = new AtomicInteger(0);
        Map<String, AtomicInteger> plays = new HashMap<>();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isApolloGame)
                .peek(match -> total.incrementAndGet())
                .filter(Predicate.not(Match::removed))
                .forEach(match -> Scenarios.fixScenarios(match.scenarios())
                        .forEach(scenario -> Maps.putIfAbsent(plays, scenario, new AtomicInteger()).incrementAndGet()));


        List<Stats> stats = new ArrayList<>();
        plays.forEach((key, times) -> {
            double percent = times.get() / (double)total.get();
            stats.add(new Stats(key, percent * 100));
            LoggerFactory.getLogger(getClass()).info("{} - {}", key, percent * 100);
        });

        stats.sort(Comparator.comparing(Stats::percent).reversed());
        return WebUtils.makeHTML("Percent", Arrays.asList("Scenarios", "Percent"), stats);
    }

    @Override
    public String getName() {
        return "percent";
    }

    public record Stats(String name, double percent) implements WebUtils.ITableData {

        private final static DecimalFormat df = new DecimalFormat("#.#####");

        @Override
        public List<String> getData() {
            return Arrays.asList(name, df.format(percent) + "%");
        }
    }
}
