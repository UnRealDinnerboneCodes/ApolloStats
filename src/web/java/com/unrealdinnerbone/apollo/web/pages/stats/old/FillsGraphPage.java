package com.unrealdinnerbone.apollo.web.pages.stats.old;

import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FillsGraphPage implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        StringBuilder builder = new StringBuilder("Time,Host,Amount\n");


        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(match -> Instant.parse(match.opens()).toEpochMilli()))
                .forEach(match -> match.findGameData().ifPresent(game -> builder.append(Instant.parse(match.opens()).toString()).append(",").append( match.author()).append(",").append(game.fill()).append("\n")));
        wrapper.html(builder.toString());
    }

    @Override
    public String getPath() {
        return "data/fills";
    }


}
