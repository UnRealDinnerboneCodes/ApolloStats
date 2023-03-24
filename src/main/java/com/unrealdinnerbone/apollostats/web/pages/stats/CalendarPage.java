package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.apollostats.lib.MyWebUtils;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import com.unrealdinnerbone.unreallib.list.Maps;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public record CalendarPage() implements IStatPage {


    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
        boolean includeStaff = wrapper.queryParam("staff").map(s -> {
            try {
                return Boolean.parseBoolean(s);
            } catch (Exception e) {
                return true;
            }
        }).orElse(true);
        Map<Integer, Map<Month, Map<Staff, AtomicInteger>>> map = new HashMap<>();
        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
            Staff staff = staffListEntry.getKey();
            List<Match> sortedMatches = staffListEntry.getValue().stream()
                    .sorted(Comparator.comparing(Match::getOpenTime)).toList();
            for (Match match : sortedMatches) {
                ZonedDateTime opens = match.getOpenTime().atZone(ZoneId.of("UTC"));
                int year = opens.get(ChronoField.YEAR);
                Month month = Month.of(opens.get(ChronoField.MONTH_OF_YEAR));
                Maps.putIfAbsent(map, year, new HashMap<>(12));
                Maps.putIfAbsent(map.get(year), month, new HashMap<>());
                Maps.putIfAbsent(map.get(year).get(month), staff, new AtomicInteger()).incrementAndGet();
            }
        }
        List<Pair<String, List<Pair<String, String>>>> cardStats = new ArrayList<>();

        for (Map.Entry<Integer, Map<Month, Map<Staff, AtomicInteger>>> integerMapEntry : map.entrySet()) {
            Integer year = integerMapEntry.getKey();
            Map<Month, Map<Staff, AtomicInteger>> monthMap = integerMapEntry.getValue();
            for (Map.Entry<Month, Map<Staff, AtomicInteger>> monthMapEntry : monthMap.entrySet()) {
                Month month = monthMapEntry.getKey();
                Map<Staff, AtomicInteger> staffMap = monthMapEntry.getValue();
                List<Pair<String, String>> staffStats = new ArrayList<>();
                int total = 0;
                for (Map.Entry<Staff, AtomicInteger> staffAtomicIntegerEntry : staffMap.entrySet()) {
                    Staff staff = staffAtomicIntegerEntry.getKey();
                    AtomicInteger integer = staffAtomicIntegerEntry.getValue();
                    if(includeStaff) {
                        String amount = integer.toString();
                        if(amount.equals("69")) {
                            amount += " (Nice)";
                        }
                        staffStats.add(Pair.of(staff.displayName(), integer.toString()));
                    }
                    total += integer.get();
                }
                staffStats.add(0, Pair.of("Total", String.valueOf(total)));
                cardStats.add(Pair.of(month.name() + " " + year, staffStats));
            }
        }


        sort(cardStats, "Month", new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] split1 = o1.split(" ");
                String[] split2 = o2.split(" ");
                int year1 = Integer.parseInt(split1[1]);
                int year2 = Integer.parseInt(split2[1]);
                String month1 = split1[0];
                String month2 = split2[0];
                if(year1 == year2) {
                    return Month.valueOf(month1).compareTo(Month.valueOf(month2));
                }
                return Integer.compare(year1, year2);
            }
        });
        if(includeStaff) {
            wrapper.html(MyWebUtils.makeCardPage("Calendar", "", "Month", new HashMap<>(), cardStats));
        }else {
            String month = "";
            List<Supplier<List<String>>> list = new ArrayList<>();
            for (Pair<String, List<Pair<String, String>>> stringListPair : cardStats) {
                month = stringListPair.key();
                String total = "";
                for (Pair<String, String> stringStringPair : stringListPair.value()) {
                    total = stringStringPair.value();
                }
                String finalMonth = month;
                String finalTotal = total;
                list.add(() -> List.of(finalMonth, finalTotal));
                wrapper.html(WebUtils.makeHtmlTable("Calendar", "", List.of("Month", "Amount"), list));
            }
        }

    }


    public static void sort(List<Pair<String, List<Pair<String, String>>>> map, String key, Comparator<String> stringComparator) {
        map.sort((o1, o2) -> {
            String value = o2.key();
            String value1 = o1.key();
            return stringComparator.compare(value, value1);
        });
    }

    @Override
    public String getPath() {
        return "calendar";
    }
}
