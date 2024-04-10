package com.unrealdinnerbone.apollo.web.pages.graph;

import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.web.api.ICTXGetter;
import com.unrealdinnerbone.unreallib.list.Maps;
import com.unrealdinnerbone.unreallib.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class GameHostedGen implements IGraphPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameHostedGen.class);


    @Override
    public List<DataSet> getGraphData(Map<Staff, List<Match>> hostMatchMap, ICTXGetter getter) {
        List<DataSet> dataSets = new ArrayList<>();
        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
            Map<Instant, AtomicInteger> amountOfGames = new HashMap<>();
            Color color = getColorForStaff(staffListEntry.getKey());
            staffListEntry.getValue().stream()
                    .filter(Match::isApolloGame)
                    .filter(Predicate.not(Match::removed))
                    .map(Match::opens)
                    .map(Instant::parse)
                    .map(instant -> instant.truncatedTo(java.time.temporal.ChronoUnit.DAYS))
                    .forEach(instant -> Maps.putIfAbsent(amountOfGames, instant, new AtomicInteger()).incrementAndGet());

            AtomicInteger totalGames = new AtomicInteger();
            List<GraphData> graphData = amountOfGames.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(instantAtomicIntegerEntry -> new GraphData(instantAtomicIntegerEntry.getKey(), totalGames.addAndGet(instantAtomicIntegerEntry.getValue().get())))
                    .toList();
            dataSets.add(new DataSet(staffListEntry.getKey().displayName(), graphData, String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())));
        }
        return dataSets;
    }


    //Todo move color to database
    public static Color getColorForStaff(Staff staff) {
        if(staff.displayName().equalsIgnoreCase("Logan")) {
            return Color.CYAN;
        }else {
            return new Color(MathHelper.randomInt(0, 255), MathHelper.randomInt(0, 255), MathHelper.randomInt(0, 255));

        }
    }

    @Override
    public String getPath() {
        return "/graphs/games";
    }
}
