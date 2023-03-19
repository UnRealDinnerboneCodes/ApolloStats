package com.unrealdinnerbone.apollostats.lib;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public record ScenarioPredicate(List<Scenario> allowed) implements Predicate<Match> {

    @Override
    public boolean test(Match match) {
        return new HashSet<>(Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios())).containsAll(allowed);
    }
}
