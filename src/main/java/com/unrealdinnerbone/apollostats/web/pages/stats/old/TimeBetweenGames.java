package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

public class TimeBetweenGames implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, Long> staffIntegerMap = new HashMap<>();
        Map<Staff, Pair<Instant, Instant>> map = new HashMap<>();


        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
            List<Match> matches = new ArrayList<>(staffListEntry.getValue());
            matches.sort(Comparator.comparing(match -> Instant.parse(match.opens())));
            for (int i = 0; i < matches.size(); i++) {
                if (i == 0) {
                    continue;
                }
                Instant match = matches.get(i).getOpenTime();
                Instant lastMatch = matches.get(i - 1).getOpenTime();
                long between = ChronoUnit.DAYS.between(lastMatch, match);
                if(!staffIntegerMap.containsKey(staffListEntry.getKey())) {
                    staffIntegerMap.put(staffListEntry.getKey(), 0L);
                }
                if(staffIntegerMap.get(staffListEntry.getKey()) < between) {
                    staffIntegerMap.put(staffListEntry.getKey(), between);
                    map.put(staffListEntry.getKey(), new Pair<>(lastMatch, match));
                }
            }
        }

        List<Data> matchPairs = new ArrayList<>();
        for (Map.Entry<Staff, Long> staffLongEntry : staffIntegerMap.entrySet()) {
            Pair<Instant, Instant> instantInstantPair = map.get(staffLongEntry.getKey());
            matchPairs.add(new Data(staffLongEntry.getKey(), instantInstantPair.key(), instantInstantPair.value(), staffLongEntry.getValue()));

        }

        wrapper.html(WebUtils.makeHTML("Times", "", Arrays.asList("Host", "From", "To", "Days"), matchPairs));
    }

    @Override
    public String getPath() {
        return "time";
    }


    public record Data(Staff staff, Instant from, Instant to, long days) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            return Arrays.asList(staff.displayName(), Util.formatData(from), Util.formatData(to), String.valueOf(days));
        }
    }
}
