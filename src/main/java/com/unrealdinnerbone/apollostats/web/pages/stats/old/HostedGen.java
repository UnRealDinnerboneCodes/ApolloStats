package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class HostedGen implements IStatPage {


    @Override
    public String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper query) {
        Map<Staff, Long> plays = new HashMap<>();

        for (Map.Entry<Staff, List<Match>> stringListEntry : hostMatchMap.entrySet()) {
            plays.put(stringListEntry.getKey(), stringListEntry.getValue().stream()
                    .filter(Match::isApolloGame)
                    .filter(Predicate.not(Match::removed))
                    .map(Match::opens)
                    .map(Instant::parse)
                            .filter(instant -> Instant.now().isAfter(instant))
                    .count());
        }



        List<HostData> hostData = new ArrayList<>(plays.entrySet().stream().map(e -> new HostData(e.getKey().displayName(), e.getValue())).toList());
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
    public String getPath() {
        return "hosted";
    }

}
