package com.unrealdinnerbone.apollo.web.pages.stats;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Scenario;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.list.Maps;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunnyScenNames implements IStatPage {

    private static final Logger LOGGER = LogHelper.getLogger();
    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {

        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .map(Match::id)
                .forEach(Stats.INSTANCE.getGameManager()::findGame);

        Map<Scenario, List<String>> scenarioToNames = new HashMap<>();
        for (Map.Entry<String, List<Scenario>> stringOptionalEntry : Stats.INSTANCE.getScenarioManager().getLazyMap().entrySet()) {
            stringOptionalEntry.getValue()
                    .forEach(scenario ->
                            Maps.putIfAbsent(scenarioToNames, scenario, new ArrayList<>()).add(stringOptionalEntry.getKey()));
        }
        for (Map.Entry<Scenario, List<String>> scenarioListEntry : scenarioToNames.entrySet()) {
            List<String> names = scenarioListEntry.getValue();
            names.removeIf(name -> Stats.INSTANCE.getScenarioManager().isSimilar(name, scenarioListEntry.getKey().name()));
            if(!names.isEmpty()) {
                LOGGER.info("Scenario: {}: {}", scenarioListEntry.getKey().name(), names);
            }
        }
    }

    @Override
    public String getPath() {
        return "lol";
    }
}
