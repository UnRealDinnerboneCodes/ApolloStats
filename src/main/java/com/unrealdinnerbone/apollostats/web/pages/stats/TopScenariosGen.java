package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.text.DecimalFormat;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TopScenariosGen implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        AtomicInteger total = new AtomicInteger(0);
        HashMap<Scenario, AtomicInteger> matchCount = new HashMap<>();

        hostMatchMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(Match::isGoodGame)
                .peek(match -> total.incrementAndGet())
                .map(match -> Scenarios.fix(Type.SCENARIO, match.scenarios()))
                .flatMap(Collection::stream)
                .forEach(scenario -> Maps.putIfAbsent(matchCount, scenario, new AtomicInteger(0)).incrementAndGet());

        List<Count> stats = new ArrayList<>();
        matchCount.forEach((key, times) -> stats.add(new Count(key.name(), times.get(), (times.get() / (double) total.get()) * 100)));


        stats.sort(Comparator.comparing(Count::percent).reversed());
        return WebUtils.makeHTML("Top Scenarios", "", Arrays.asList("Scenario", "Count", "Percent"), stats);
    }

    @Override
    public String getName() {
        return "top";
    }


    public record Count(String scenario, int amount, double percent) implements Supplier<List<String>> {

        private final static DecimalFormat df = new DecimalFormat("#.#####");

        @Override
        public List<String> get() {
            return Arrays.asList(scenario, String.valueOf(amount), df.format(percent) + "%");
        }
    }

}
