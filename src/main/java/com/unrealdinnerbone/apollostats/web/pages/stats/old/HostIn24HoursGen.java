package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.TimeUtil;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HostIn24HoursGen implements IStatPage {


    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, Pair<List<Match>, AtomicReference<Instant>>> max = hostMatchMap
                .keySet()
                .stream()
                .collect(Collectors.toMap(s -> s, s -> Pair.of(new ArrayList<>(), new AtomicReference<>(Instant.EPOCH)), (a, b) -> b));


        for (Map.Entry<Staff, List<Match>> stringListEntry : hostMatchMap.entrySet()) {
            List<Match> times = new ArrayList<>(stringListEntry.getValue());
            times.sort(Comparator.comparing(Match::getOpenTime));
            Staff staff = stringListEntry.getKey();
            for (int i = 0; i < times.size(); i++) {
                Match time = times.get(i);
                List<Match> matchesAfter = new ArrayList<>();
                Instant startTime = time.getOpenTime();
                for (int j = i; j < times.size(); j++) {
                    Match instant = times.get(j);
                    if (TimeUtil.isWithinFrom(startTime, instant.getOpenTime(), 24, ChronoUnit.HOURS)) {
                        matchesAfter.add(instant);
                    }
                }
                if (max.get(staff).key().size() < matchesAfter.size()) {
                    max.get(staff).key().clear();
                    max.get(staff).key().addAll(matchesAfter);
                    max.get(staff).value().set(startTime);
                }
            }
        }

        List<HostData> cake = new ArrayList<>(max.entrySet()
                .stream()
                .map(entry -> new HostData(entry.getKey().displayName(), entry.getValue().key().size(), entry.getValue().value().get())).toList());

        cake.sort(Comparator.comparing(HostData::i).reversed());
        wrapper.html(WebUtils.makeHTML("Hosts in 24 hours", "", Arrays.asList("Host", "Times", "Date"), cake));
    }

    public record HostData(String s, int i, Instant date) implements Supplier<List<String>> {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
                .withLocale(Locale.UK)
                .withZone(ZoneId.of("UTC"));


        @Override
        public List<String> get() {
            return Arrays.asList(s, String.valueOf(i), formatter.format(date));
        }
    }


    @Override
    public String getPath() {
        return "host24";
    }

}
