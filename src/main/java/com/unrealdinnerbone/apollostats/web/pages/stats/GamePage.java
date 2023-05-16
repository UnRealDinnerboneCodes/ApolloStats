package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.list.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GamePage implements IStatPage {

    @Override
    public boolean filterMatches(Match match) {
        return match.isApolloGame();
    }

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Long, Pair<List<Scenario>, AtomicInteger>> listAtomicIntegerMap = new HashMap<>();
        for (List<Match> matches : hostMatchMap.values()) {
            for (Match match : matches) {
                List<Scenario> fix = Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios());
                fix.removeIf(scenario -> {
                    return scenario.name().equalsIgnoreCase("rush");
                });
                fix.sort(Comparator.comparing(Scenario::name));
                long hash = fix.hashCode();
                Maps.putIfAbsent(listAtomicIntegerMap, hash, new Pair<>(fix, new AtomicInteger(0))).value().incrementAndGet();
            }
        }

        List<GameStats> gameStats = new ArrayList<>();
        listAtomicIntegerMap.forEach((key, value) -> gameStats.add(new GameStats(value.key(), value.value())));
        wrapper.html(WebUtils.makeHtmlTable("Games", "", Arrays.asList("Scens", "Count"), gameStats));

    }

    public record GameStats(List<Scenario> scenarios, AtomicInteger count) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            return List.of(scenarios.stream().map(Scenario::name).collect(Collectors.joining(", ")), String.valueOf(count.get()));
        }
    }

    @Override
    public String getPath() {
        return "game";
    }
}
