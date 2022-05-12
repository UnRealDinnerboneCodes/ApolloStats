package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class TimeBetweenGames implements IStatPage {

    @Override
    public String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper query) {
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

        return WebUtils.makeHTML("Times", "", Arrays.asList("One", "Two", "Diff"), matchPairs.stream().sorted(Comparator.comparing(pair -> (Instant.parse(pair.before().opens()).toEpochMilli() - Instant.parse(pair.after().opens()).toEpochMilli()))).toList());
    }

    @Override
    public String getPath() {
        return "time";
    }


    public record Data(Match before, Match after) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            return Arrays.asList(before.opens(), after.opens(), String.valueOf(Instant.parse(after().opens()).toEpochMilli() - Instant.parse(before().opens()).toEpochMilli()));
        }
    }
}
