package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HostIn24HoursGen implements IStatPage {


    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, List<Instant>> plays = new HashMap<>();

        for (Map.Entry<Staff, List<Match>> stringListEntry : hostMatchMap.entrySet()) {
            plays.put(stringListEntry.getKey(), stringListEntry.getValue().stream()
                    .filter(Match::isApolloGame)
                    .filter(Predicate.not(Match::removed))
                    .map(Match::opens)
                            .map(Instant::parse)
                    .toList());
        }


        Map<Staff, Pair<AtomicInteger, Instant>> max = hostMatchMap.keySet().stream().collect(Collectors.toMap(s -> s, s -> Pair.of(new AtomicInteger(0), Instant.EPOCH), (a, b) -> b));
        Pair<Integer, Instant> apollo = Pair.of(0, Instant.EPOCH);

        for (Map.Entry<Staff, List<Instant>> stringListEntry : plays.entrySet()) {
            List<Instant> times = new ArrayList<>(stringListEntry.getValue());
            times.sort(Comparator.reverseOrder());
            for (Instant time : times) {
                int theTimes = 0;
                Instant lastTime = null;
                Instant time1 = Instant.parse(time.toString());


                for (Instant instant : times) {

                    Instant instant1 = Instant.parse(instant.toString());

                    boolean isWithinPrior24Hours = (!instant1.isBefore(time1.minus(24, ChronoUnit.HOURS))) && (instant1.isBefore(time1.plus(1, ChronoUnit.MILLIS)));
                    if(isWithinPrior24Hours) {
                        theTimes++;
                        lastTime = instant1;
                    }
                }
                if(lastTime != null) {
                    max.put(stringListEntry.getKey(), Pair.of(new AtomicInteger(theTimes), lastTime));
                    if(apollo.key() < theTimes) {
                        apollo = Pair.of(theTimes, lastTime);
                    }
                }
            }
        }




        List<HostData> cake = new ArrayList<>(max.entrySet().stream().map(entry -> new HostData(entry.getKey().displayName(), entry.getValue().key().get(), entry.getValue().value())).toList());
        cake.add(new HostData("Apollo", apollo.key(), apollo.value()));

        wrapper.html(WebUtils.makeHTML("Hosts in 24 hours", "https://unreal.codes/kevStonk.png", Arrays.asList("Host", "Times"), cake));
    }

    public record HostData(String s, int i, Instant date) implements Supplier<List<String>> {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
                .withLocale(Locale.UK)
                .withZone(ZoneId.of("UTC"));


        @Override
        public List<String> get() {
            return Arrays.asList(s, String.valueOf(i), formatter.format(date));
        }
    }


    @Override
    public String getPath() {
        return "cheese";
    }

}
