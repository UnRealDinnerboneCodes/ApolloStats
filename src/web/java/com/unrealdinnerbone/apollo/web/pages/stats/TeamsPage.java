package com.unrealdinnerbone.apollo.web.pages.stats;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.core.api.Type;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import com.unrealdinnerbone.unreallib.list.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TeamsPage implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
        Map<String, AtomicInteger> amount = getTeamMap(hostMatchMap);
        List<Team> uniqueTeams = amount.entrySet().stream()
                .map(entry -> new Team(entry.getKey(), entry.getValue().get()))
                .toList();
        wrapper.html(WebUtils.makeHtmlTable("teams", "", List.of("Type", "Amount"), uniqueTeams));
    }

    public static Map<String, AtomicInteger> getTeamMap(Map<Staff, List<Match>> hostMatchMap) {
        Map<String, AtomicInteger> amount = new HashMap<>();
        for (Match match : hostMatchMap.values().stream().flatMap(List::stream).toList()) {
            int teamSize = match.getTeamSize();
            int teamAmount = match.getTeamAmount();
            Stats.INSTANCE.getScenarioManager().fix(Type.TEAM, Collections.singletonList(match.getTeamFormat())).forEach(scenario -> {
                StringBuilder stringBuilder = new StringBuilder(scenario.name());
                if(teamSize != 0) {
                    stringBuilder.append(" (").append(teamSize).append(")");
                }
                if(teamAmount != 0) {
                    stringBuilder.append(" [").append(teamAmount).append("]");
                }
                Maps.putIfAbsent(amount, stringBuilder.toString(), new AtomicInteger(0)).incrementAndGet();
            });

        }
        return amount;
    }

    public record Team(String id, int amount) implements Supplier<List<String>> {

        @Override
        public List<String> get() {
            return List.of(id, String.valueOf(amount));
        }
    }

    @Override
    public String getPath() {
        return "testteams";
    }
}
