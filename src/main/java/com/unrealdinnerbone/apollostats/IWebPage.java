package com.unrealdinnerbone.apollostats;

import java.util.List;
import java.util.Map;

public interface IWebPage {
    String generateStats(Map<String, List<Match>> hostMatchMap);

    String getName();
}
