package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.MyWebUtils;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.TimeUtil;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import com.unrealdinnerbone.unreallib.list.LazyHashMap;
import com.unrealdinnerbone.unreallib.list.Maps;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class HostPage implements IStatPage {

    private final static DecimalFormat FORMAT = new DecimalFormat("#.##");

    private static final Logger LOGGER = LogHelper.getLogger();
    public static final LazyHashMap<String, Cache<Staff, CachedData>> DAYS_IN_A_ROW = new LazyHashMap<>((s) -> CacheBuilder.newBuilder().build());

    @Override
    public boolean filterMatches(Match match) {
        return true;
    }

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
        List<Pair<String, List<Pair<String, String>>>> cardStats = new ArrayList<>();
        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
            cardStats.add(new Pair<>(staffListEntry.getKey().displayName(), createFor(staffListEntry.getKey(), new ArrayList<>(staffListEntry.getValue()), wrapper.getRequestID())));
        }
        List<Match> allMatches = new ArrayList<>(hostMatchMap.values()
                .stream()
                .flatMap(Collection::stream)
                .toList());

        List<Pair<String, String>> aFor = createFor(Staff.APOLLO, allMatches, wrapper.getRequestID());
        cardStats.add(new Pair<>("Apollo", aFor));

        Map<String, String> sortMap = new HashMap<>();
        BiFunction<String, String, String> urlCreator = (key, value) -> {
            Map<String, List<String>> thePerms = new HashMap<>(wrapper.getQueryPerms());
            thePerms.put(key, List.of(value));
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, List<String>> stringListEntry : thePerms.entrySet()) {
                builder.append(stringListEntry.getKey()).append("=").append(String.join(",", stringListEntry.getValue())).append("&");
            }
            return "hosts?" + builder.substring(0, builder.length() - 1);
        };

        sortMap.put("Games Hosted", urlCreator.apply("sort", "gamesHosted"));
        sortMap.put("Games Removed", urlCreator.apply("sort", "gamesRemoved"));
        sortMap.put("Nether On", urlCreator.apply("sort", "netherOn"));
        sortMap.put("Rush", urlCreator.apply("sort", "rush"));
        sortMap.put("Top Scenario", urlCreator.apply("sort", "topScenario"));
        sortMap.put("Top Team Type", urlCreator.apply("sort", "topTeamType"));
        sortMap.put("Days in a Row", urlCreator.apply("sort", "daysInARow"));
        sortMap.put("Hosts in 24 Hours", urlCreator.apply("sort", "hostsIn24Hours"));
        sortMap.put("Fill (Min)", urlCreator.apply("sort", "fillMin"));
        sortMap.put("Fill (Max)", urlCreator.apply("sort", "fillMax"));
        sortMap.put("Fill (Avg)", urlCreator.apply("sort", "fillAvg"));
        sortMap.put("Host Gap", urlCreator.apply("sort", "hostGap"));

        wrapper.queryParam("sort").ifPresent(s -> {
            switch (s.toLowerCase()) {
                case "gameshosted" -> sort(cardStats, "Games Hosted", Comparator.comparingInt(Integer::parseInt));
                case "gamesremoved" -> sort(cardStats, "Games Removed", Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0])));
                case "netheron" -> sort(cardStats, "Nether On", Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0])));
                case "rush" -> sort(cardStats, "Rush", Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0])));
                case "topscenario" -> sort(cardStats, "Top Scenario", Comparator.comparing(value -> value));
                case "topteamtype" -> sort(cardStats, "Top Team Type", Comparator.comparing(value -> value));
                case "daysinarow" -> sort(cardStats, "Days in a Row", Comparator.comparingInt(Integer::parseInt));
                case "hostsin24hours" -> sort(cardStats, "Hosts in 24 Hours", Comparator.comparingInt(Integer::parseInt));
                case "fillmin" -> sort(cardStats, "Fill (Min, Max, Avg)", Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0])));
                case "fillmax" -> sort(cardStats, "Fill (Min, Max, Avg)", Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[2])));
                case "fillavg" -> sort(cardStats, "Fill (Min, Max, Avg)", Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[4])));
                case "hostgap" -> sort(cardStats, "Host Gap (Days)", Comparator.comparingInt(value -> {
                    String[] s1 = value.split(" ");
                    return Integer.parseInt(s1[7].replace(")", "").replace("(", ""));
                }));
            }
        });

        wrapper.html(MyWebUtils.makeCardPage("Stats", "", sortMap, cardStats));
    }


    public static List<Pair<String, String>> createFor(Staff staff, List<Match> matches, String requestID) {
        List<Pair<String, String>> stats = new ArrayList<>();
        int gamesHosted = 0;
        int gamesRemoved = 0;
        int netherOn = 0;
        int rush = 0;
        Map<Scenario, AtomicInteger> scenarioCount = new HashMap<>();
        Map<String, AtomicInteger> teamCount = new HashMap<>();

        Staff key = staff;
        List<Match> value = matches;
        CachedData cachedData = getMostMatches(requestID, key, value.stream().filter(Match::isGoodGame).toList());
        List<Integer> fills = new ArrayList<>();
        for (Match match : value.stream().sorted(Comparator.comparing(Match::getOpenTime)).toList()) {
            if(match.isGoodGame()) {
                Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios())
                        .forEach(scenario -> Maps.putIfAbsent(scenarioCount, scenario, new AtomicInteger()).incrementAndGet());
                Stats.INSTANCE.getScenarioManager().fix(Type.TEAM, Collections.singletonList(match.getTeamFormat()))
                        .forEach(scenario -> {
                            StringBuilder stringBuilder = new StringBuilder(scenario.name());
                            int teamSize = match.getTeamSize();
                            if(teamSize != 0) {
                                stringBuilder.append(" (").append(teamSize).append(")");
                            }
                            int teamAmount = match.getTeamAmount();
                            if(teamAmount != 0) {
                                stringBuilder.append(" [").append(teamAmount).append("]");
                            }
                            Maps.putIfAbsent(teamCount, stringBuilder.toString(), new AtomicInteger()).incrementAndGet();
                        });
                gamesHosted++;
                if(match.isRush()) {
                    rush++;
                }
                if(match.isNetherEnabled()) {
                    netherOn++;
                }
                Stats.INSTANCE.getGameManager().findGame(match.id()).ifPresent(game -> fills.add(game.fill()));
            }
            if(match.isApolloGame() && match.removed()) {
                gamesRemoved++;
            }
        }
        int mostHostedScen = 0;
        int mostHostedTeam = 0;
        Scenario mostHostedScenario = null;
        String mostHostedTeamType = null;
        for (Map.Entry<Scenario, AtomicInteger> entry : scenarioCount.entrySet()) {
            if(entry.getValue().get() > mostHostedScen && entry.getKey().type() == Type.SCENARIO) {
                if(!entry.getKey().meta()) {
                    mostHostedScen = entry.getValue().get();
                    mostHostedScenario = entry.getKey();
                }
            }
        }
        for (Map.Entry<String, AtomicInteger> entry : teamCount.entrySet()) {
            if(entry.getValue().get() > mostHostedTeam) {
                mostHostedTeam = entry.getValue().get();
                mostHostedTeamType = entry.getKey();
            }
        }
        int maxFill = fills.stream().max(Integer::compareTo).orElse(0);
        int minFill = fills.stream().min(Integer::compareTo).orElse(0);
        int avgFill = fills.size() == 0 ? 0 : fills.stream().mapToInt(Integer::intValue).sum() / fills.size();

        stats.add(Pair.of("Games Hosted", String.valueOf(gamesHosted)));
        stats.add(Pair.of("Games Removed", gamesRemoved + " (" + FORMAT.format((double) gamesRemoved / (double) gamesHosted * 100) + "%)"));
        stats.add(Pair.of("Nether On", netherOn + " (" + FORMAT.format((double) netherOn / (double) gamesHosted * 100) + "%)"));
        stats.add(Pair.of("Rush", rush + " (" + FORMAT.format((double) rush / (double) gamesHosted * 100) + "%)"));
        stats.add(Pair.of("Top Scenario", mostHostedScenario == null ? "None" : mostHostedScenario.name() + " (" + mostHostedScen + " / " + FORMAT.format((double) mostHostedScen / (double) gamesHosted * 100) + "%)"));
        stats.add(Pair.of("Top Team Type", mostHostedTeamType == null ? "None" : mostHostedTeamType + " (" + mostHostedTeam + " / " + FORMAT.format((double) mostHostedTeam / (double) gamesHosted * 100) + "%)"));
        stats.add(Pair.of("Days in a Row", String.valueOf(cachedData.daysInARow().size())));
        stats.add(Pair.of("Hosts in 24 Hours", String.valueOf(cachedData.hostIn24Hours().size())));
        stats.add(Pair.of("Fill (Min, Max, Avg)", minFill + " / " + maxFill + " / " + avgFill));
        String fromDate = Util.formatData(cachedData.timeBetween().key());
        String toDate = Util.formatData(cachedData.timeBetween().value());
        int daysBetween = (int) ChronoUnit.DAYS.between(cachedData.timeBetween().key(), cachedData.timeBetween().value());
        stats.add(Pair.of("Host Gap (Days)", fromDate + " -> " + toDate + " (" + daysBetween + ")"));
        return stats;
    }

    public <E> void sort(List<Pair<String, List<Pair<String, String>>>> map, String key, Comparator<String> stringComparator) {
        map.sort((o1, o2) -> {
            String value = null;
            String value1 = null;
            for (Pair<String, String> stringStringPair : o2.value()) {
                if(stringStringPair.key().equals(key)) {
                    value = stringStringPair.value();
                }
            }
            for (Pair<String, String> stringStringPair : o1.value()) {
                if(stringStringPair.key().equals(key)) {
                    value1 = stringStringPair.value();
                }
            }
            return stringComparator.compare(value, value1);
        });
    }

    @Override
    public String getPath() {
        return "hosts";
    }

    public static List<Cache<Staff, CachedData>> getCaches() {
        return DAYS_IN_A_ROW.values().stream()
                .toList();
    }

    private static CachedData getMostMatches(String id, Staff staff, List<Match> matches) {
        try {
            return DAYS_IN_A_ROW.get(id).get(staff, () -> {
                List<Match> daysInARow = new ArrayList<>();
                List<Match> hostIn24Hours = new ArrayList<>();
                List<Match> sortedMatches = matches.stream().sorted(Comparator.comparing(Match::getOpenTime)).toList();
                Pair<Instant, Instant> timeBetween = new Pair<>(Instant.EPOCH, Instant.EPOCH);
                int daysBetween = 0;
                for (int i = 0; i < sortedMatches.size(); i++) {
                    Match match = sortedMatches.get(i);
                    {
                        if(i != sortedMatches.size() - 1) {
                            Instant nowMatch = sortedMatches.get(i).getOpenTime();
                            Instant lastMatch = sortedMatches.get(i + 1).getOpenTime();
                            long between1 = ChronoUnit.DAYS.between(nowMatch, lastMatch);
                            if(between1 < 0) {
                                LOGGER.info("Between is less than 0, adding 1");
                            }
                            long between = Math.abs(between1);

                            if(daysBetween < between) {
                                daysBetween = (int) between;
                                timeBetween = Pair.of(nowMatch, lastMatch);
                            }
                        }
                    }
                    {
                        List<Match> daysInARowAfter = new ArrayList<>();
                        Instant currentTime = match.getOpenTime();
                        for (int j = i; j < sortedMatches.size(); j++) {
                            Match match1 = sortedMatches.get(j);
                            Instant startTime = match1.getOpenTime();
                            if (startTime.isAfter(currentTime)) {
                                if (TimeUtil.isWithinFrom(currentTime, startTime, 24, ChronoUnit.HOURS)) {
                                    daysInARowAfter.add(match1);
                                    currentTime = startTime;
                                } else {
                                    break;
                                }
                            }
                        }
                        if (daysInARow.size() < daysInARowAfter.size()) {
                            daysInARow.clear();
                            daysInARow.addAll(daysInARowAfter);
                        }
                    }
                    {
                        List<Match> host24InARowAfter = new ArrayList<>();
                        Instant startTime = match.getOpenTime();
                        for (int j = i; j < sortedMatches.size(); j++) {
                            Match instant = sortedMatches.get(j);
                            if (TimeUtil.isWithinFrom(startTime, instant.getOpenTime(), 24, ChronoUnit.HOURS)) {
                                host24InARowAfter.add(instant);
                            }else {
                                break;
                            }
                        }
                        if (hostIn24Hours.size() < host24InARowAfter.size()) {
                            hostIn24Hours.clear();
                            hostIn24Hours.addAll(host24InARowAfter);
                        }
                    }


                }
                return new CachedData(daysInARow, hostIn24Hours, timeBetween);
            });
        } catch (ExecutionException e) {
            LOGGER.error("Error getting most matches", e);
            return new CachedData(Collections.emptyList(), Collections.emptyList(), new Pair<>(Instant.EPOCH, Instant.EPOCH));
        }
    }

    public record CachedData(List<Match> daysInARow, List<Match> hostIn24Hours, Pair<Instant, Instant> timeBetween) { }
}
