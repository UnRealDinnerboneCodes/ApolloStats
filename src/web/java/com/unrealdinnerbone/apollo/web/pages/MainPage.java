package com.unrealdinnerbone.apollo.web.pages;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;

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
