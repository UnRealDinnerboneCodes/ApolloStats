package com.unrealdinnerbone.apollostats.web.pages;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;

import java.util.List;
import java.util.Map;

public class MainPage implements IStatPage {
    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        wrapper.html(Stats.getResourceAsString("main.html"));
    }

    @Override
    public String getPath() {
        return "";
    }
}
