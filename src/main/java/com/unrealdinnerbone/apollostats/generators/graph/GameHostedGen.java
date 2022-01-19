package com.unrealdinnerbone.apollostats.generators.graph;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class GameHostedGen  implements IWebPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameHostedGen.class);

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        List<Instant> times  = hostMatchMap.values().stream().flatMap(List::stream).filter(Match::isApolloGame).filter(Predicate.not(Match::removed)).map(Match::opens).map(Instant::parse).sorted().toList();
        StringBuilder builder = new StringBuilder("Time,Host,Amount\n");
        Map<String, Integer> lastHostedAmount = new HashMap<>();
        for(String s : hostMatchMap.keySet()) lastHostedAmount.put(s, -1);
        for(Instant time : times) {
            time = time.plus(1, ChronoUnit.SECONDS);
            Map<String, AtomicInteger> gamesHosted = new HashMap<>();
            for(Map.Entry<String, List<Match>> stringListEntry : hostMatchMap.entrySet()) {
                String host = stringListEntry.getKey();
                gamesHosted.put(host, new AtomicInteger(0));
                for(Match match : stringListEntry.getValue()) {
                    if(match.isApolloGame() && !match.removed() && Instant.parse(match.opens()).isBefore(time)) {
                        gamesHosted.get(host).incrementAndGet();
                    }
                }
            }
            for(Map.Entry<String, AtomicInteger> stringAtomicIntegerEntry : gamesHosted.entrySet()) {
                if(lastHostedAmount.get(stringAtomicIntegerEntry.getKey()) != stringAtomicIntegerEntry.getValue().get()) {
                    LOGGER.info("{} hosted {} games at {}", stringAtomicIntegerEntry.getKey(), stringAtomicIntegerEntry.getValue().get(), time);
                    builder.append(time.toString()).append(",").append(stringAtomicIntegerEntry.getKey()).append(",").append(stringAtomicIntegerEntry.getValue().get()).append("\n");
                }
                lastHostedAmount.put(stringAtomicIntegerEntry.getKey(), stringAtomicIntegerEntry.getValue().get());
            }
        }
        return builder.toString();
    }

    @Override
    public String getName() {
        return "games_hosted.csv";
    }
}
