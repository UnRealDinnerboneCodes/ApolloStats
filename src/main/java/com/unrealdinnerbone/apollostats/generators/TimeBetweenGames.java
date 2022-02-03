package com.unrealdinnerbone.apollostats.generators;

import com.unrealdinnerbone.apollostats.IWebPage;
import com.unrealdinnerbone.apollostats.Match;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import javax.swing.*;
import java.time.Instant;
import java.util.*;

public class TimeBetweenGames implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        List<Match> matches = hostMatchMap.values().stream().flatMap(List::stream).toList().stream().sorted(Comparator.comparing(match -> {
            return Instant.parse(match.opens());
        })).toList();

        Match lastMatch = null;
        List<Data> matchPairs = new ArrayList<>();
        for(Match match : matches) {
            if(lastMatch == null) {
                lastMatch = match;
            }
            matchPairs.add(new Data(lastMatch, match));
            lastMatch = match;
        }

        return WebUtils.makeHTML("Times", Arrays.asList("One", "Two", "Diff"), matchPairs.stream().sorted(Comparator.comparing(pair -> (Instant.parse(pair.before().opens()).toEpochMilli() - Instant.parse(pair.after().opens()).toEpochMilli()))).toList());
    }

    @Override
    public String getName() {
        return "time";
    }


    public record Data(Match before, Match after) implements WebUtils.ITableData {
        @Override
        public List<String> getData() {
            return Arrays.asList(before.opens(), after.opens(), String.valueOf(Instant.parse(after().opens()).toEpochMilli() - Instant.parse(before().opens()).toEpochMilli()));
        }
    }
}
