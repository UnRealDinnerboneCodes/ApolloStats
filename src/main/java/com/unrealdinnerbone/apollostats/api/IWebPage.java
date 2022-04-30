package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.apollostats.web.ApolloRole;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IWebPage {

    default String generateStats(Map<String, List<Match>> hostMatchMap, Function<String, String> query) {
        return generateStats(hostMatchMap);
    }
    String generateStats(Map<String, List<Match>> hostMatchMap);

    String getName();


    default ApolloRole getRole() {
        return ApolloRole.EVERYONE;
    }
}
