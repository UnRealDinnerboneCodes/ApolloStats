package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HostIn24HoursGen implements IWebPage {


    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        Map<String, List<Instant>> plays = new HashMap<>();

        for (Map.Entry<String, List<Match>> stringListEntry : hostMatchMap.entrySet()) {
            plays.put(stringListEntry.getKey(), stringListEntry.getValue().stream()
                    .filter(Match::isApolloGame)
                    .filter(Predicate.not(Match::removed))
                    .map(Match::opens)
                            .map(Instant::parse)
                    .toList());
        }


        Map<String, AtomicInteger> max = hostMatchMap.keySet().stream().collect(Collectors.toMap(s -> s, s -> new AtomicInteger(0), (a, b) -> b));

        for (Map.Entry<String, List<Instant>> stringListEntry : plays.entrySet()) {
            List<Instant> times = new ArrayList<>(stringListEntry.getValue());
            times.sort(Comparator.reverseOrder());
            for (Instant time : times) {
                int theTimes = 0;
                Instant time1 = Instant.parse(time.toString());


                for (Instant instant : times) {

                    Instant instant1 = Instant.parse(instant.toString());

                    boolean isWithinPrior24Hours =
                            ( ! instant1.isBefore( time1.minus( 24 , ChronoUnit.HOURS) ) )
                                    &&
                                    ( instant1.isBefore( time1.plus(1, ChronoUnit.MILLIS) )
                                    ) ;


                        if(isWithinPrior24Hours) {
                            theTimes++;
                        }
                }
                max.get(stringListEntry.getKey()).set(Math.max(max.get(stringListEntry.getKey()).get(), theTimes));
            }
        }

        List<HostData> cake = max.entrySet().stream().map(entry -> new HostData(entry.getKey(), entry.getValue().get())).toList();


        return WebUtils.makeHTML("Hosts in 24 hours", "https://unreal.codes/kevStonk.png", Arrays.asList("Host", "Times"), cake);
    }

    public record HostData(String s, int i) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            return Arrays.asList(s, String.valueOf(i));
        }
    }


    @Override
    public String getName() {
        return "cheese";
    }

}
