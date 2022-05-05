package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HostedGen implements IWebPage {


    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        Map<String, Long> plays = new HashMap<>();

        for (Map.Entry<String, List<Match>> stringListEntry : hostMatchMap.entrySet()) {
            plays.put(stringListEntry.getKey(), stringListEntry.getValue().stream()
                    .filter(Match::isApolloGame)
                    .filter(Predicate.not(Match::removed))
                    .map(Match::opens)
                    .map(Instant::parse)
                            .filter(instant -> Instant.now().isAfter(instant))
                    .count());
        }



        List<HostData> hostData = plays.entrySet().stream().map(e -> new HostData(e.getKey(), e.getValue())).toList();
        int total = hostData.stream().map(HostData::i).mapToInt(Math::toIntExact).sum();
        hostData.add(new HostData("ApolloUHC", total));

        return WebUtils.makeHTML("Hosts in 24 hours", "https://unreal.codes/kevStonk.png", Arrays.asList("Host", "Times"), hostData);
    }

    public record HostData(String s, long i) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            return Arrays.asList(s, String.valueOf(i));
        }
    }


    @Override
    public String getName() {
        return "hosted";
    }

}
