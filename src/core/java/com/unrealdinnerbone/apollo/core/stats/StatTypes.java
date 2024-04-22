package com.unrealdinnerbone.apollo.core.stats;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Scenario;
import com.unrealdinnerbone.apollo.core.api.Type;
import com.unrealdinnerbone.apollo.core.lib.Util;
import com.unrealdinnerbone.apollo.core.stats.types.GamesPlayedStat;
import com.unrealdinnerbone.apollo.core.stats.types.TimeBetweenStat;
import com.unrealdinnerbone.unreallib.StringUtils;
import com.unrealdinnerbone.unreallib.TimeUtil;
import com.unrealdinnerbone.unreallib.list.LazyHashMap;
import com.unrealdinnerbone.unreallib.list.Maps;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class StatTypes
{

    private static final LazyHashMap<IStatType<?>, Cache<String, ?>> CACHE = new LazyHashMap<>(iStatType -> CacheBuilder.newBuilder().build());

    public static <T> T getStat(IStatType<T> statType, String name, List<Match> matches) throws IllegalStateException {
        Cache<String, T> stringCache = (Cache<String, T>) CACHE.get(statType);
        try {
            return stringCache.get(name, () -> statType.getStat(matches));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final IStatType<String> TIME_BETWEEN = create(matches -> {
        List<Match> sortedMatches = matches
                .stream()
                .filter(Match::isGoodGame)
                .sorted(Comparator.comparing(Match::getOpenTime)).toList();
        TimeBetweenStat timeBetween = new TimeBetweenStat(Instant.EPOCH, Instant.EPOCH);
        int daysBetween = 0;
        for (int i = 0; i < sortedMatches.size(); i++) {
            Match match = sortedMatches.get(i);
            {
                if(i != sortedMatches.size() - 1) {
                    Instant nowMatch = match.getOpenTime();
                    Instant lastMatch = sortedMatches.get(i + 1).getOpenTime();

                    long between1 = ChronoUnit.DAYS.between(nowMatch, lastMatch);
                    long between = Math.abs(between1);


                    if(daysBetween < between) {
                        daysBetween = (int) between;
                        timeBetween = new TimeBetweenStat(nowMatch, lastMatch);
                    }
                }
            }
        }
        String format = Util.formatData(timeBetween.from()) + " -> " + Util.formatData(timeBetween.to());
        return StringUtils.replace("{0} ({1})", timeBetween.getBetween(ChronoUnit.DAYS), format);
    });

    public static final IStatType<String> DAYS_IN_A_ROW = create(matches -> {
        List<Match> sortedMatches = matches.stream()
                .filter(Match::isGoodGame)
                .sorted(Comparator.comparing(Match::getOpenTime)).toList();
        List<Match> daysInARow = new ArrayList<>();
        for (int i = 0; i < sortedMatches.size(); i++) {
            Match match = sortedMatches.get(i);
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
        if(daysInARow.isEmpty()) {
            return "0";
        }
        Instant from = daysInARow.get(0).getOpenTime();
        Instant to = daysInARow.get(daysInARow.size() - 1).getOpenTime();
        String format = Util.formatData(from) + " -> " + Util.formatData(to);
        return daysInARow.size() + " (" + format + ")";
    });

    public static final IStatType<String> HOST_IN_24_HOURS = create(matches -> {
        List<Match> hostIn24Hours = new ArrayList<>();
        List<Match> sortedMatches = matches
                .stream()
                .filter(Match::isGoodGame)
                .sorted(Comparator.comparing(Match::getOpenTime)).toList();

        for (int i = 0; i < sortedMatches.size(); i++) {
            Match match = sortedMatches.get(i);
            List<Match> host24InARowAfter = new ArrayList<>();
            Instant startTime = match.getOpenTime();
            for (int j = i; j < sortedMatches.size(); j++) {
                Match instant = sortedMatches.get(j);
                if (TimeUtil.isWithinFrom(startTime, instant.getOpenTime(), 24, ChronoUnit.HOURS)) {
                    host24InARowAfter.add(instant);
                } else {
                    break;
                }
            }
            if (hostIn24Hours.size() < host24InARowAfter.size()) {
                hostIn24Hours.clear();
                hostIn24Hours.addAll(host24InARowAfter);
            }
        }
        Instant from = hostIn24Hours.get(0).getOpenTime();
        Instant to = hostIn24Hours.get(hostIn24Hours.size() - 1).getOpenTime();
        String format = Util.formatData(from) + " -> " + Util.formatData(to);
        return hostIn24Hours.size() + " (" + format + ")";
    });

    public static final IStatType<GamesPlayedStat> GAMES_PLAYED = create(matches -> {
        int gamesHosted = 0;
        int gamesRemoved = 0;
        int netherOn = 0;
        int rush = 0;
        int totalMatches = 0;
        Map<Scenario, AtomicInteger> scenarioCount = new HashMap<>();
        Map<String, AtomicInteger> teamCount = new HashMap<>();
        List<Integer> fills = new ArrayList<>();
        for (Match match : matches) {
            if (match.isGoodGame()) {
                com.unrealdinnerbone.apollo.core.Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios())
                        .forEach(scenario -> Maps.putIfAbsent(scenarioCount, scenario, new AtomicInteger()).incrementAndGet());
                com.unrealdinnerbone.apollo.core.Stats.INSTANCE.getScenarioManager().fix(Type.TEAM, Collections.singletonList(match.getTeamFormat()))
                        .forEach(scenario -> {
                            StringBuilder stringBuilder = new StringBuilder(scenario.name());
                            int teamSize = match.getTeamSize();
                            if (teamSize != 0) {
                                stringBuilder.append(" (").append(teamSize).append(")");
                            }
                            int teamAmount = match.getTeamAmount();
                            if (teamAmount != 0) {
                                stringBuilder.append(" [").append(teamAmount).append("]");
                            }
                            Maps.putIfAbsent(teamCount, stringBuilder.toString(), new AtomicInteger()).incrementAndGet();
                        });
                gamesHosted++;
                if (match.isRush()) {
                    rush++;
                }
                if (match.isNetherEnabled()) {
                    netherOn++;
                }
                com.unrealdinnerbone.apollo.core.Stats.INSTANCE.getGameManager().findGame(match.id()).ifPresent(game -> fills.add(game.fill()));
            }
            if (match.isApolloGame()) {
                totalMatches++;
                if (match.removed()) {
                    gamesRemoved++;
                }
            }
        }
        return new GamesPlayedStat(totalMatches, gamesHosted, gamesRemoved, netherOn, rush, scenarioCount, teamCount, fills);
    });

    public static IStatType<Integer> GAMES_HOSTED = create((matches) -> (int) matches.stream().filter(Match::isGoodGame).count());
    public static IStatType<String> GAMES_REMOVED = create((matches) -> GAMES_PLAYED.getStat(matches).getGamesRemoved());
    public static IStatType<String> NETHER_ON = create((matches) -> GAMES_PLAYED.getStat(matches).getNetherOn());

    public static IStatType<String> RUSH = create((matches) -> GAMES_PLAYED.getStat(matches).getRush());

    public static IStatType<String> TOP_SCENARIO = create((matches) -> GAMES_PLAYED.getStat(matches).getTopScenario());

    public static IStatType<String> TOP_TEAM_FORMAT = create((matches) -> GAMES_PLAYED.getStat(matches).getTopTeamType());

    public static IStatType<String> FILL = create((matches) -> {
        GamesPlayedStat gamesPlayedStat = GAMES_PLAYED.getStat(matches);
        return StringUtils.replace("{0} / {1} / {2}", gamesPlayedStat.getMinFill(), gamesPlayedStat.getMaxFill(), gamesPlayedStat.getAverageFill());
    });


    private static <T> IStatType<T> create(Function<List<Match>, T> function) {
        return function::apply;
    }
}
