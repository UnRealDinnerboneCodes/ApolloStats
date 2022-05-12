package com.unrealdinnerbone.apollostats.web.pages.graph;

import com.unrealdinnerbone.apollostats.api.*;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class FillsGraphPage implements IStatPage {

    @Override
    public String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper query) {
        StringBuilder builder = new StringBuilder("Time,Host,Amount\n");


        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(match -> Instant.parse(match.opens()).toEpochMilli()))
                .forEach(match -> match.findGameData().ifPresent(game -> builder.append(Instant.parse(match.opens()).toString()).append(",").append( match.author()).append(",").append(game.fill()).append("\n")));
        return builder.toString();
    }

    @Override
    public String getPath() {
        return "data/fills";
    }


}
