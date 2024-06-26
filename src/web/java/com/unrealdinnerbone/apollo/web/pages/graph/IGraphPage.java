package com.unrealdinnerbone.apollo.web.pages.graph;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.web.api.ICTXGetter;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;
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
            dataData.add(new DataData(dataSet.label(), mapData, "none", dataSet.backgroundColor()));
        }
        String page = Stats.getResourceAsString("graph.html");
        String json = JsonUtil.DEFAULT.toFancyJson(dataData);
        wrapper.html(page.replace("{\"DATA\"}", json));
    }

    List<DataSet> getGraphData(Map<Staff, List<Match>> hostMatchMap, ICTXGetter getter);

    record DataSet(String label, List<GraphData> graphData, String backgroundColor) {}
    record GraphData(Instant time, int amount) {}

    record MapData(String t, int y) {}

    record DataData(String label, List<MapData> data, String fill, String backgroundColor) {}
}
