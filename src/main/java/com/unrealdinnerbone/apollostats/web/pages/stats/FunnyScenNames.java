package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.mangers.GameManager;
import com.unrealdinnerbone.apollostats.mangers.ScenarioManager;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.Maps;
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
                .filter(Match::isGoodGame)
                .filter(Match::isApolloGame)
                .map(Match::id)
                .forEach(GameManager::findGame);

        Map<Scenario, List<String>> scenarioToNames = new HashMap<>();
        for (Map.Entry<String, List<Scenario>> stringOptionalEntry : ScenarioManager.getLazyMap().entrySet()) {
            stringOptionalEntry.getValue()
                    .forEach(scenario ->
                            Maps.putIfAbsent(scenarioToNames, scenario, new ArrayList<>()).add(stringOptionalEntry.getKey()));
        }
        for (Map.Entry<Scenario, List<String>> scenarioListEntry : scenarioToNames.entrySet()) {
            List<String> names = scenarioListEntry.getValue();
            names.removeIf(name -> ScenarioManager.isSimilar(name, scenarioListEntry.getKey().name()));
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
