package com.unrealdinnerbone.apollo.web.pages.stats.hosts;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Scenario;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.core.api.Type;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;
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
