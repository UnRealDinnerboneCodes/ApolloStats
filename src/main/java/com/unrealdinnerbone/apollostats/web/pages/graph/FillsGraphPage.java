package com.unrealdinnerbone.apollostats.web.pages.graph;

import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.Match;

import java.time.Instant;
import java.util.*;

public class FillsGraphPage implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        StringBuilder builder = new StringBuilder("Time,Host,Amount\n");


        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(match -> Instant.parse(match.opens()).toEpochMilli()))
                .forEach(match -> match.findGameData().ifPresent(game -> builder.append(Instant.parse(match.opens()).toString()).append(",").append( match.author()).append(",").append(game.fill()).append("\n")));
        return builder.toString();
    }

    @Override
    public String getName() {
        return "data/fills";
    }


}
