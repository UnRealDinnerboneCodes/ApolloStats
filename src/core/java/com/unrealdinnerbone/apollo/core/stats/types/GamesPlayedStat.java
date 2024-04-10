package com.unrealdinnerbone.apollo.core.stats.types;


import com.unrealdinnerbone.apollo.core.api.Scenario;
import com.unrealdinnerbone.apollo.core.api.Type;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public record GamesPlayedStat(int totalPost,
                              int hosted,

                              int removed,
                              int nether,
                              int rush,
                              Map<Scenario, AtomicInteger> scenario,
                              Map<String, AtomicInteger> teamCount,
                              List<Integer> fills) {

    private final static DecimalFormat FORMAT = new DecimalFormat("#.##");

    public int getMaxFill() {
        return fills.stream().max(Integer::compareTo).orElse(0);
    }

    public int getMinFill() {
        return fills.stream().min(Integer::compareTo).orElse(0);
    }

    public int getAverageFill() {
        return fills.size() == 0 ? 0 : fills.stream().mapToInt(Integer::intValue).sum() / fills.size();
    }

    public Scenario getMostPopularScenario() {
        int mostHostedScen = 0;
        Scenario mostHostedScenario = null;
        for (Map.Entry<Scenario, AtomicInteger> entry : scenario.entrySet()) {
            if(entry.getValue().get() > mostHostedScen && entry.getKey().type() == Type.SCENARIO) {
                if(!entry.getKey().meta()) {
                    mostHostedScen = entry.getValue().get();
                    mostHostedScenario = entry.getKey();
                }
            }
        }
        return mostHostedScenario;
    }

    public String getMostPopularTeam() {
        int mostHostedTeam = 0;
        String mostHostedTeamType = null;
        for (Map.Entry<String, AtomicInteger> entry : teamCount.entrySet()) {
            if(entry.getValue().get() > mostHostedTeam) {
                mostHostedTeam = entry.getValue().get();
                mostHostedTeamType = entry.getKey();
            }
        }
        return mostHostedTeamType;
    }

    public String getGamesRemoved() {
        return removed + " (" + FORMAT.format((double) removed / (double) hosted * 100) + "%)";
    }

    public String getNetherOn() {
        return nether + " (" + FORMAT.format((double) nether / (double) hosted * 100) + "%)";
    }

    public String getRush() {
        return rush + " (" + FORMAT.format((double) rush / (double) hosted * 100) + "%)";
    }

    public String getTopScenario() {
        if (getMostPopularScenario() == null) {
            return "None";
        } else {
            int amount = scenario.get(getMostPopularScenario()).get();
            return getMostPopularScenario().name() + " (" + amount + " / " + FORMAT.format((double) amount / (double) hosted * 100) + "%)";
        }
    }

    public String getTopTeamType() {
        if (getMostPopularTeam() == null) {
            return "None";
        } else {
            int amount = teamCount.get(getMostPopularTeam()).get();
            return getMostPopularTeam() + " (" + amount + " / " + FORMAT.format((double) amount / (double) hosted * 100) + "%)";
        }
    }

    public int getTeamCount(String type) {
        return teamCount.getOrDefault(type, new AtomicInteger(0)).get();
    }

    public int getScenarioCount(Scenario scenario) {
        return this.scenario.getOrDefault(scenario, new AtomicInteger(0)).get();
    }
}
