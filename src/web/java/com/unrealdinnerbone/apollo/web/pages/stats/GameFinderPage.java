package com.unrealdinnerbone.apollo.web.pages.stats;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Scenario;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.core.api.Type;
import com.unrealdinnerbone.apollo.core.lib.Util;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;
import com.unrealdinnerbone.unreallib.StringUtils;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import com.unrealdinnerbone.unreallib.list.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GameFinderPage implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
        List<String> scens = Arrays.stream(wrapper.queryParam("scens")
                        .map(s -> s.split(","))
                        .orElseThrow(() -> new WebResultException("No Scens", 400)))
                .toList();

        List<Scenario> wantedScens = Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, scens);


        Map<Staff, List<Match>> foundsMatches = new HashMap<>();
        Predicate<Match> filter = match -> new HashSet<>(Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios())).containsAll(wantedScens);
        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
            List<Match> matches = staffListEntry.getValue();
            matches.stream().filter(filter)
                    .forEach(match -> Maps.putIfAbsent(foundsMatches, staffListEntry.getKey(), new ArrayList<>()).add(match));
        }

        List<GameStats> gameStats = new ArrayList<>();
        foundsMatches.forEach((key, value) -> value.stream().map(match -> new GameStats(key, match)).forEach(gameStats::add));
        wrapper.html(WebUtils.makeHtmlTable("Games", "", Arrays.asList("Host", "Time", "Scens", "Game"), gameStats));
    }


    public record GameStats(Staff host, Match match) implements Supplier<List<String>> {


        @Override
        public List<String> get() {
            List<String> data = new ArrayList<>();
            data.add(host.displayName());
            data.add(Util.formatData(match.getOpenTime()));
            data.add(String.join(", ", match.scenarios()));
            data.add(StringUtils.replace("<a href=\\\"https://hosts.uhc.gg/m/{0}\\\">{0}</a>\"", match.id()));
            return data;
        }
    }

    @Override
    public String getPath() {
        return "games";
    }
}
