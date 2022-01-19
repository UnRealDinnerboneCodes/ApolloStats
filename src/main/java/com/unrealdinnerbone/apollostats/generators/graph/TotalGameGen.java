package com.unrealdinnerbone.apollostats.generators.graph;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class TotalGameGen implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        StringBuilder builder = new StringBuilder("Time,Amount\n");
        AtomicInteger amount = new AtomicInteger();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isApolloGame)
                .filter(Predicate.not(Match::removed))
                .map(Match::opens)
                .map(Instant::parse)
                .sorted(Comparator.comparing(Instant::toEpochMilli))
                        .forEach(instant -> builder.append(instant).append(",").append(amount.incrementAndGet()).append("\n"));
        return builder.toString();
    }

    @Override
    public String getName() {
        return "total_game";
    }
}
