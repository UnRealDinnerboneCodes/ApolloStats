package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

public class DaysInARowPage implements IStatPage {

    private static final Logger LOGGER = LogHelper.getLogger();
    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, List<Match>> daysHostedInARow = new HashMap<>();
        hostMatchMap.forEach((staff, matches) -> {
            List<Match> mostMatches = new ArrayList<>();
            for (Match match : matches.stream().sorted(Comparator.comparing(Match::getOpenTime)).toList()) {
                List<Match> matchesAfter = new ArrayList<>();
                Instant currentTime = match.getOpenTime();
                for (Match match1 : matches.stream().sorted(Comparator.comparing(Match::getOpenTime)).toList()) {
                    if (match1.getOpenTime().isAfter(currentTime)) {
                        int between = (int) ChronoUnit.HOURS.between(currentTime, match1.getOpenTime());
                        between = Math.abs(between);
                        if (between <= 24) {
                            matchesAfter.add(match1);
                            currentTime = match1.getOpenTime();
                        }else {
                            break;
                        }
                    }
                }
                if(matchesAfter.size() > mostMatches.size()) {
                    mostMatches = matchesAfter;
                }
            }
            if(mostMatches.size() >= 1) {
                daysHostedInARow.put(staff, mostMatches);
            }
        });
        List<RowStats> rowStats = new ArrayList<>();
        daysHostedInARow.forEach((staff, pair) -> rowStats.add(new RowStats(staff, pair)));
        wrapper.html(WebUtils.makeHTML("Days in a row", "", List.of("Host", "First", "Last", "Days", "Amount"), rowStats));
    }
    @Override
    public String getPath() {
        return "days-in-a-row";
    }

    public record RowStats(Staff name, List<Match> matches) implements Supplier<List<String>> {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
                .withLocale(Locale.UK)
                .withZone(ZoneId.of("UTC"));

        @Override
        public List<String> get() {
            Instant first = matches.get(0).getOpenTime();
            Instant last = matches.get(matches.size() - 1).getOpenTime();
            long between = ChronoUnit.DAYS.between(first, last);
            int amount = matches.size();
            return List.of(name.displayName(), formatter.format(first), formatter.format(last), String.valueOf(between), String.valueOf(amount));
        }
    }
}
