//package com.unrealdinnerbone.apollostats.web.pages.stats.old;
//
//import com.unrealdinnerbone.apollostats.api.*;
//import com.unrealdinnerbone.apollostats.mangers.ScenarioManager;
//import com.unrealdinnerbone.unreallib.Maps;
//import com.unrealdinnerbone.unreallib.web.WebUtils;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Function;
//import java.util.function.Predicate;
//import java.util.function.Supplier;
//
//public class LongTimeGames implements IStatPage {
//
//    @Override
//    public String generateStats(Map<Staff, List<Match>> hostMatchMap,ICTXWrapper query) {
//        Map<String, Map<LTGType, AtomicInteger>> theMap = new HashMap<>();
//
//
//        hostMatchMap.forEach((host, matches) -> matches.stream()
//                .filter(Match::isApolloGame)
//                .filter(Predicate.not(Match::removed))
//                .forEach(match -> LTGType.getType(match)
//                        .ifPresent(type -> Maps.putIfAbsent(Maps.putIfAbsent(theMap, match.author(), new HashMap<>()), type, new AtomicInteger(0)).incrementAndGet())));
//
//        List<Types> types =  theMap.entrySet().stream().map(entry -> {
//            int edr = entry.getValue().getOrDefault(LTGType.EDR, new AtomicInteger(0)).get();
//            int skyhigh = entry.getValue().getOrDefault(LTGType.SKYHIGH, new AtomicInteger(0)).get();
//            int slo = entry.getValue().getOrDefault(LTGType.SLO, new AtomicInteger(0)).get();
//            int nether = entry.getValue().getOrDefault(LTGType.NETHER, new AtomicInteger(0)).get();
//            int fallout = entry.getValue().getOrDefault(LTGType.FALLOUT, new AtomicInteger(0)).get();
//            return new Types(entry.getKey(), edr, skyhigh, slo, nether, fallout);
//        }).toList();
//        return WebUtils.makeHTML("Long Time Games", "",Arrays.asList("Host", "EDR", "SkyHigh", "SOL", "Nether", "Fallout"), types);
//    }
//
//    @Override
//    public String getPath() {
//        return "ltg";
//    }
//
//
//    public record Types(String host, int edr, int skyhigh, int slo, int nether, int fallout) implements Supplier<List<String>> {
//        @Override
//        public List<String> get() {
//            return Arrays.asList(host, String.valueOf(edr), String.valueOf(skyhigh), String.valueOf(slo), String.valueOf(nether), String.valueOf(fallout));
//        }
//    }
//
//    public enum LTGType {
//        EDR("Ender Dragon Rush"),
//        SKYHIGH("Sky High"),
//        SLO("Slice of Life"),
//        NETHER("Nether Meetup", "Go To Hell"),
//        FALLOUT("Fallout");
//
//        private final String[] name;
//
//        LTGType(String... name) {
//            this.name = name;
//        }
//
//        public static Optional<LTGType> getType(Match match) {
//            for(String fixScen : ScenarioManager.fix(Type.SCENARIO, match.scenarios()).stream().map(Scenario::name).toList()) {
//                for(LTGType type : LTGType.values()) {
//                    for(String name : type.getNames()) {
//                        if(fixScen.equalsIgnoreCase(name)) {
//                            return Optional.of(type);
//                        }
//                    }
//                }
//            }
//            return Optional.empty();
//        }
//
//        public String[] getNames() {
//            return name;
//        }
//    }
//}
