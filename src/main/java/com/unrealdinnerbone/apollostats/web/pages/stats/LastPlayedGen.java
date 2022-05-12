package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class LastPlayedGen implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        Map<Scenario, List<Pair<Instant, String>>> plays = new HashMap<>();
        AtomicInteger totalGames = new AtomicInteger();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isGoodGame)
                .peek(match -> totalGames.incrementAndGet())
                .forEach(match -> Scenarios.fix(Type.SCENARIO, match.scenarios())
                        .stream()
                        .filter(Scenario::official)
                        .toList()
                        .forEach(scenario -> Maps.putIfAbsent(plays, scenario, new ArrayList<>()).add(Pair.of(Instant.parse(match.opens()), match.displayName()))));

        List<Stats> stats = new ArrayList<>();

//        long totalScens = plays.values().stream().map(List::size).mapToInt(Integer::intValue).sum();
        plays.forEach((key, times) -> {
            times.sort(Comparator.comparing(Pair::key));
            double percent = times.size() / (double) totalGames.get();
            stats.add(new Stats(key.name(), times.get(0), times.get(times.size() - 1), times.size(), percent));
        });

        stats.sort(Comparator.comparing(stats1 -> stats1.last().key()));
        return WebUtils.makeHTML("Scenarios First / Last Played", "", Arrays.asList("Scenarios", "First Time", "Last Time", "First Host", "Last Host", "Days Since", "Times Hosted", "Percent Hosted"), stats);
    }

    @Override
    public String getName() {
        return "played";
    }

    public record Stats(String name, Pair<Instant, String> first, Pair<Instant, String> last, int timesPlayed, double percent) implements Supplier<List<String>> {


        private final static DecimalFormat df = new DecimalFormat("#.##");

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC"));

        @Override
        public List<String> get() {
            long between = ChronoUnit.DAYS.between( last.key(), Instant.now());
            return Arrays.asList(name, formatter.format(first.key()), formatter.format(last.key()), first.value(), last.value(), "Days: " + String.format("%03d", between), String.valueOf(timesPlayed), df.format(percent * 100) + "%");
        }
    }
}
