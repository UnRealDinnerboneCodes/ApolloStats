package com.unrealdinnerbone.apollostats.web.pages.graph;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.json.JsonUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IGraphPage extends IStatPage {

    @Override
    default void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {

        List<DataSet> data = getGraphData(hostMatchMap, wrapper);
        List<DataData> dataData = new ArrayList<>();

        for (DataSet dataSet : data) {
            List<MapData> mapData = dataSet.graphData().stream()
                    .map(graphData -> new MapData(graphData.time.toString(), graphData.amount()))
                    .toList();
            dataData.add(new DataData(dataSet.label(), mapData, dataSet.backgroundColor()));
        }
        String page = Stats.getResourceAsString("graph.html");
        String json = JsonUtil.DEFAULT.toFancyJson(List.class, dataData);
        wrapper.html(page.replace("{\"DATA\"}", json));
    }

    List<DataSet> getGraphData(Map<Staff, List<Match>> hostMatchMap, ICTXGetter getter);

    record DataSet(String label, List<GraphData> graphData, String backgroundColor) {}
    record GraphData(Instant time, int amount) {}

    record MapData(String t, int y) {}

    record DataData(String label, List<MapData> data, String backgroundColor) {}
}
