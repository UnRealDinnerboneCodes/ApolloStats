package com.unrealdinnerbone.apollostats.stats;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.lib.CachedStat;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.stats.types.GamesPlayedStat;
import com.unrealdinnerbone.apollostats.stats.types.TimeBetweenStat;
import com.unrealdinnerbone.unreallib.StringUtils;
import com.unrealdinnerbone.unreallib.TimeUtil;
import com.unrealdinnerbone.unreallib.list.Maps;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedStats {

    private final static DecimalFormat FORMAT = new DecimalFormat("#.##");

    public static final CachedStat<String> TIME_BETWEEN = new CachedStat<>((id, staff, matches) -> {
        List<Match> sortedMatches = matches.stream()
                .filter(Match::isGoodGame)
                .sorted(Comparator.comparing(Match::getOpenTime)).toList();
        TimeBetweenStat timeBetween = new TimeBetweenStat(Instant.EPOCH, Instant.EPOCH);
        int daysBetween = 0;
        for (int i = 0; i < sortedMatches.size(); i++) {
            Match match = sortedMatches.get(i);
            {
                if (i != sortedMatches.size() - 1) {
                    Instant nowMatch = match.getOpenTime();
                    Instant lastMatch = sortedMatches.get(i + 1).getOpenTime();
                    long between1 = ChronoUnit.DAYS.between(nowMatch, lastMatch);
                    long between = Math.abs(between1);

                    if (daysBetween < between) {
                        daysBetween = (int) between;
                        timeBetween = new TimeBetweenStat(nowMatch, lastMatch);
                    }
                }
            }
        }
        String format = Util.formatData(timeBetween.from()) + " -> " + Util.formatData(timeBetween.to());
        return StringUtils.replace("{0} ({1})", timeBetween.getBetween(ChronoUnit.DAYS), format);
    });

    public static final CachedStat<String> DAYS_IN_A_ROW = new CachedStat<>((id, staff, matches) -> {
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

    public static final CachedStat<String> HOST_IN_24_HOURS = new CachedStat<>((id, staff, matches) -> {
        List<Match> hostIn24Hours = new ArrayList<>();
        List<Match> sortedMatches = matches.stream()
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

    public static final CachedStat<GamesPlayedStat> GAMES_PLAYED = new CachedStat<>((id, staff, matches) -> {
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
                Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios())
                        .forEach(scenario -> Maps.putIfAbsent(scenarioCount, scenario, new AtomicInteger()).incrementAndGet());
                Stats.INSTANCE.getScenarioManager().fix(Type.TEAM, Collections.singletonList(match.getTeamFormat()))
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
                Stats.INSTANCE.getGameManager().findGame(match.id()).ifPresent(game -> fills.add(game.fill()));
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

    public static CachedStat<Integer> GAMES_HOSTED = new CachedStat<>((id, staff, matches) -> (int) matches.stream().filter(Match::isGoodGame).count());
    public static CachedStat<String> GAMES_REMOVED = new CachedStat<>((id, staff, matches) -> GAMES_PLAYED.get(id, staff, matches).getGamesRemoved());
    public static CachedStat<String> NETHER_ON = new CachedStat<>((id, staff, matches) -> GAMES_PLAYED.get(id, staff, matches).getNetherOn());

    public static CachedStat<String> RUSH = new CachedStat<>((id, staff, matches) -> GAMES_PLAYED.get(id, staff, matches).getRush());

    public static CachedStat<String> TOP_SCENARIO = new CachedStat<>((id, staff, matches) -> GAMES_PLAYED.get(id, staff, matches).getTopScenario());

    public static CachedStat<String> TOP_TEAM_FORMAT = new CachedStat<>((id, staff, matches) -> GAMES_PLAYED.get(id, staff, matches).getTopTeamType());

    public static CachedStat<String> FILL = new CachedStat<>((id, staff, matches) -> {
        GamesPlayedStat gamesPlayedStat = GAMES_PLAYED.get(id, staff, matches);
        return StringUtils.replace("{0} / {1} / {2}", gamesPlayedStat.getMinFill(), gamesPlayedStat.getMaxFill(), gamesPlayedStat.getAverageFill());
    });


}
