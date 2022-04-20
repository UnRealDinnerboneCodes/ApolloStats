package com.unrealdinnerbone.apollostats.generators;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LongTimeGames implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        Map<String, Map<Type, AtomicInteger>> theMap = new HashMap<>();


        hostMatchMap.forEach((host, matches) -> matches.stream()
                .filter(Match::isApolloGame)
                .filter(Predicate.not(Match::removed))
                .forEach(match -> Type.getType(match)
                        .ifPresent(type -> Maps.putIfAbsent(Maps.putIfAbsent(theMap, match.author(), new HashMap<>()), type, new AtomicInteger(0)).incrementAndGet())));

        List<Types> types =  theMap.entrySet().stream().map(entry -> {
            int edr = entry.getValue().getOrDefault(Type.EDR, new AtomicInteger(0)).get();
            int skyhigh = entry.getValue().getOrDefault(Type.SKYHIGH, new AtomicInteger(0)).get();
            int slo = entry.getValue().getOrDefault(Type.SLO, new AtomicInteger(0)).get();
            int nether = entry.getValue().getOrDefault(Type.NETHER, new AtomicInteger(0)).get();
            int fallout = entry.getValue().getOrDefault(Type.FALLOUT, new AtomicInteger(0)).get();
            return new Types(entry.getKey(), edr, skyhigh, slo, nether, fallout);
        }).toList();
        return WebUtils.makeHTML("Long Time Games", "",Arrays.asList("Host", "EDR", "SkyHigh", "SOL", "Nether", "Fallout"), types);
    }

    @Override
    public String getName() {
        return "ltg";
    }


    public record Types(String host, int edr, int skyhigh, int slo, int nether, int fallout) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            return Arrays.asList(host, String.valueOf(edr), String.valueOf(skyhigh), String.valueOf(slo), String.valueOf(nether), String.valueOf(fallout));
        }
    }

    public enum Type {
        EDR("Ender Dragon Rush"),
        SKYHIGH("Sky High"),
        SLO("Slice of Life"),
        NETHER("Nether Meetup", "Go To Hell"),
        FALLOUT("Fallout");

        private final String[] name;

        Type(String... name) {
            this.name = name;
        }

        public static Optional<Type> getType(Match match) {
            for(String fixScen : Scenarios.fix(Scenarios.Type.SCENARIO, match.scenarios())) {
                for(Type type : Type.values()) {
                    for(String name : type.getNames()) {
                        if(fixScen.equalsIgnoreCase(name)) {
                            return Optional.of(type);
                        }
                    }
                }
            }
            return Optional.empty();
        }

        public String[] getNames() {
            return name;
        }
    }
}
