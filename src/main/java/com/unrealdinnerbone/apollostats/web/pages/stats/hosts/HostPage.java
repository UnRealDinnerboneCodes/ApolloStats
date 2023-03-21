package com.unrealdinnerbone.apollostats.web.pages.stats.hosts;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.exception.WebResultException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HostPage implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
        List<Staff> staffs = hostMatchMap.keySet().stream().toList();
        List<Match> matches = hostMatchMap.values().stream().flatMap(List::stream).toList();
        Map<Scenario, AtomicInteger> count = Stats.INSTANCE.getScenarioManager().getCount(Type.SCENARIO, matches);
    }

    @Override
    public String getPath() {
        return "host";
    }
}
