package com.unrealdinnerbone.apollo.web.pages.graph;

import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.web.api.ICTXGetter;
import com.unrealdinnerbone.unreallib.list.Maps;

import java.awt.*;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TotalGameGen implements IGraphPage {

    @Override
    public List<DataSet> getGraphData(Map<Staff, List<Match>> hostMatchMap, ICTXGetter getter) {
        Map<Instant, AtomicInteger> amountOfGames = new HashMap<>();
        Color color = Color.CYAN.darker().darker().darker();
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .map(Match::opens)
                .map(Instant::parse)
                .map(instant -> instant.truncatedTo(java.time.temporal.ChronoUnit.DAYS))
                .forEach(instant -> Maps.putIfAbsent(amountOfGames, instant, new AtomicInteger()).incrementAndGet());

        AtomicInteger totalGames = new AtomicInteger();
        List<GraphData> graphData = amountOfGames.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(instantAtomicIntegerEntry -> new GraphData(instantAtomicIntegerEntry.getKey(), totalGames.addAndGet(instantAtomicIntegerEntry.getValue().get())))
                .toList();
        return Collections.singletonList(new DataSet("Total Games", graphData, String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())));
    }

    @Override
    public String getPath() {
        return "graphs/got";
    }
}
