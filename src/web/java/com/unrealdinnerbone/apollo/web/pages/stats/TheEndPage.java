package com.unrealdinnerbone.apollo.web.pages.stats;

import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import com.unrealdinnerbone.unreallib.list.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public record TheEndPage() implements IStatPage {

    @Override
    public boolean filterMatches(Match match) {
        return true;
    }

//    @Override
//    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
//        Map<Staff, GamesPlayedStat> gamesPlayedStatMap = new HashMap<>();
//        Map<Staff, List<Match>> goodSTaffMap = new HashMap<>();
//        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
//            List<Match> matches = new ArrayList<>();
//            for (Match match : staffListEntry.getValue()) {
//                if(match.isGoodGame()) {
//                    matches.add(match);
//                }
//            }
//            goodSTaffMap.put(staffListEntry.getKey(), matches);
//        }
//        gamesPlayedStatMap.put(Staff.APOLLO, new GamesPlayedStat(0, 0, 0 ,0, 0, new HashMap<>(), new HashMap<>(), new ArrayList<>()));
//        for (Staff staff : Stats.INSTANCE.getStaffManager().getStaff()) {
//            List<Match> matches = goodSTaffMap.get(staff);
//            if(matches == null) {
//                matches = new ArrayList<>();
//            }
//
//            GamesPlayedStat gamesPlayed = CachedStats.GAMES_PLAYED.get(staff.displayName(), staff, matches);
//            gamesPlayedStatMap.put(staff, gamesPlayed);
//            GamesPlayedStat apollo = gamesPlayedStatMap.get(Staff.APOLLO);
//            Map<Scenario, AtomicInteger> scenarioMap = new HashMap<>(gamesPlayed.scenario());
//            scenarioMap.putAll(apollo.scenario());
//            Map<String, AtomicInteger> teamCountMap = new HashMap<>(gamesPlayed.teamCount());
//            teamCountMap.putAll(apollo.teamCount());
//            gamesPlayedStatMap.put(Staff.APOLLO, new GamesPlayedStat(apollo.totalPost() + gamesPlayed.totalPost(),
//                    apollo.hosted() + gamesPlayed.hosted(),
//                    apollo.removed() + gamesPlayed.removed(),
//                    apollo.nether() + gamesPlayed.nether(),
//                    apollo.rush() + gamesPlayed.rush(),
//                    scenarioMap,
//                    teamCountMap,
//                    gamesPlayed.fills()));
//        }
//
//        List<Stat> stats = new ArrayList<>();
//
//        GamesPlayedStat apolloStats = gamesPlayedStatMap.get(Staff.APOLLO);
//
//        Scenario topScenario = apolloStats.getMostPopularScenario();
//        int topScenarioCount = apolloStats.getScenarioCount(topScenario);
//
//        String topTeamType = apolloStats.getTopTeamType();
//        int topTeamTypeCount = apolloStats.getTeamCount(topTeamType);
//
//        stats.add(new Stat("Total Games", String.valueOf(apolloStats.totalPost()), ""));
//
//        stats.add(new Stat("Total Rush Games", String.valueOf(apolloStats.rush()), formatPercentage(apolloStats.totalPost(), apolloStats.rush())));
//        stats.add(new Stat("Total Non-Rush Games", String.valueOf(apolloStats.totalPost() - apolloStats.rush()), formatPercentage(apolloStats.totalPost(), apolloStats.totalPost() - apolloStats.rush())));
//        stats.add(new Stat("Total Nether Games", String.valueOf(apolloStats.nether()), formatPercentage(apolloStats.totalPost(), apolloStats.nether())));
//        stats.add(new Stat("Total Non-Nether Games", String.valueOf(apolloStats.totalPost() - apolloStats.nether()), formatPercentage(apolloStats.totalPost(), apolloStats.totalPost() - apolloStats.nether())));
//        stats.add(new Stat("Top Scenario", topScenario == null ? "None" : topScenario.name(), formatPercentage(apolloStats.totalPost(), topScenarioCount)));
//        stats.add(new Stat("Top Team Type", topTeamType == null ? "None" : topTeamType, formatPercentage(apolloStats.totalPost(), topTeamTypeCount)));
//
//
//
//
//
//        StringBuilder builder = new StringBuilder();
//        for (Stat stat : stats) {
//            StringBuilder append = builder.append(stat.name()).append(": ").append(stat.value());
//            if(stat.extraInfo() != null && !stat.extraInfo().isEmpty()) {
//                append = append.append(" (").append(stat.extraInfo()).append(")<br>");
//            }
//            builder = append.append("<br>");
//
//        }
//        wrapper.html(builder.toString());
//    }


    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
        Map<String, AtomicInteger> gamePerVersion = new HashMap<>();
        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
            for (Match match : staffListEntry.getValue()) {
                Maps.putIfAbsent(gamePerVersion, String.valueOf(match.mapSize()), new AtomicInteger()).incrementAndGet();
            }
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, AtomicInteger> stringAtomicIntegerEntry : gamePerVersion.entrySet()) {
            builder.append(stringAtomicIntegerEntry.getKey()).append(": ").append(stringAtomicIntegerEntry.getValue()).append("<br>");
        }
        wrapper.html(builder.toString());
    }

    private static String formatPercentage(double total, double value) {
        if(total == 0) {
            return "(0%)";
        }
        return String.format("%.2f", (value / total) * 100) + "%";
    }

    @Override
    public String getPath() {
        return "theend";
    }

    public record Stat(String name, String value, String extraInfo) { }
}
