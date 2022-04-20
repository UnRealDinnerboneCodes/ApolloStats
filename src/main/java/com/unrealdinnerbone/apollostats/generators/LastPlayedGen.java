package com.unrealdinnerbone.apollostats.generators;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LastPlayedGen implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        Map<String, List<Pair<Instant, String>>> plays = new HashMap<>();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isApolloGame)
                .filter(Predicate.not(Match::removed))
                .forEach(match -> Scenarios.fix(Scenarios.Type.SCENARIO, match.scenarios())
                        .forEach(scenario -> Maps.putIfAbsent(plays, scenario, new ArrayList<>()).add(Pair.of(Instant.parse(match.opens()), match.hostingName()))));

        List<Stats> stats = new ArrayList<>();
        plays.forEach((key, times) -> {
            times.sort(Comparator.comparing(Pair::key));
            stats.add(new Stats(key, times.get(0), times.get(times.size() - 1)));
        });

        stats.sort(Comparator.comparing(stats1 -> stats1.last().key()));
        return WebUtils.makeHTML("Scenarios First / Last Played", "", Arrays.asList("Scenarios", "First Time", "Last Time", "First Host", "Last Host"), stats);
    }

    @Override
    public String getName() {
        return "played";
    }

    public record Stats(String name, Pair<Instant, String> first, Pair<Instant, String> last) implements Supplier<List<String>> {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC"));

        @Override
        public List<String> get() {
            return Arrays.asList(name, formatter.format(first.key()), formatter.format(last.key()), first.value(), last.value());
        }
    }
}
