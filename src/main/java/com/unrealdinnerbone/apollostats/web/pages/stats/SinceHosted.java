package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.ScenarioManager;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

public class SinceHosted implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Scenario, List<Instant>> hosted = new HashMap<>();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isGoodGame)
                .forEach(match -> ScenarioManager.fix(Type.SCENARIO, match.scenarios())
                        .stream()
                        .filter(Scenario::official)
                        .toList()
                        .forEach(scenario -> Maps.putIfAbsent(hosted, scenario, new ArrayList<>()).add(Instant.parse(match.opens()))));


        List<Stats> stats = new ArrayList<>();
        ScenarioManager.getValues(Type.SCENARIO).stream().filter(Scenario::official).forEach(scenario -> {
            String name = scenario.name();
            if(hosted.containsKey(scenario)) {
                List<Instant> times = hosted.get(scenario);
                times.sort(Comparator.comparing(Instant::toEpochMilli));
                stats.add(new Stats(name, times.get(0), times.get(times.size() -1)));
            }else {
                stats.add(new Stats(name, null, null));
            }
        });
        wrapper.html(WebUtils.makeHTML("Scenarios First / Last Played", "", Arrays.asList("Scenarios", "First Time", "Last Time", "Days Since First", "Day Since Last"), stats));
    }

    @Override
    public String getPath() {
        return "since";
    }

    public record Stats(String name, @Nullable Instant first, @Nullable Instant last) implements Supplier<List<String>> {


        private final static DecimalFormat df = new DecimalFormat("#.##");

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC"));

        @Override
        public List<String> get() {
            long betweenFirst = ChronoUnit.DAYS.between( first, Util.utcNow());
            long betweenLast = ChronoUnit.DAYS.between( last, Util.utcNow());
            return Arrays.asList(name, formatter.format(first), formatter.format(last), "Days: " + String.format("%03d", betweenFirst), "Days: " + String.format("%03d", betweenLast));
        }
    }
}
