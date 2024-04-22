package com.unrealdinnerbone.apollo.web.pages.stats.hosts;

import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.core.stats.IStatType;
import com.unrealdinnerbone.apollo.core.stats.StatTypes;
import com.unrealdinnerbone.apollo.web.api.ICTXWrapper;
import com.unrealdinnerbone.apollo.web.api.IStatPage;
import com.unrealdinnerbone.apollo.web.util.MyWebUtils;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.StringUtils;
import com.unrealdinnerbone.unreallib.exception.WebResultException;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class HostsPage implements IStatPage {
    private static final List<Stat<?>> stats = new ArrayList<>();

    static {
        stats.add(new Stat<>("Games Hosted", StatTypes.GAMES_HOSTED, Comparator.comparingInt(Integer::parseInt)));
        stats.add(new Stat<>("Games Removed", StatTypes.GAMES_REMOVED, Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0]))));
        stats.add(new Stat<>("Nether On", StatTypes.NETHER_ON, Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0]))));
        stats.add(new Stat<>("Rush", StatTypes.RUSH, Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0]))));
        stats.add(new Stat<>("Top Scenario", StatTypes.TOP_SCENARIO, Comparator.comparing(value -> value)));
        stats.add(new Stat<>("Top Team Type", StatTypes.TOP_TEAM_FORMAT, Comparator.comparing(value -> value)));
        stats.add(new Stat<>("Days in a Row", StatTypes.DAYS_IN_A_ROW, Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0]))));
        stats.add(new Stat<>("Host in 24 Hours", StatTypes.HOST_IN_24_HOURS, Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0]))));
        stats.add(new Stat<>("Fills", StatTypes.FILL, Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0]))));
        stats.add(new Stat<>("Host Gap (Days)", StatTypes.TIME_BETWEEN, Comparator.comparingInt(value -> Integer.parseInt(value.split(" ")[0]))));
    }

    @Override
    public boolean filterMatches(Match match) {
        return true;
    }

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException {
        List<Pair<String, List<Pair<String, String>>>> cardStats = new ArrayList<>();
        for (Map.Entry<Staff, List<Match>> staffListEntry : hostMatchMap.entrySet()) {
            cardStats.add(new Pair<>(staffListEntry.getKey().displayName(), createFor(staffListEntry.getKey(), new ArrayList<>(staffListEntry.getValue()), wrapper.getRequestID())));
        }
        List<Match> allMatches = new ArrayList<>(hostMatchMap.values()
                .stream()
                .flatMap(Collection::stream)
                .toList());

        List<Pair<String, String>> aFor = createFor(Staff.APOLLO, allMatches, wrapper.getRequestID());
        cardStats.add(new Pair<>("Apollo", aFor));

        Map<String, String> sortMap = new HashMap<>();
        BiFunction<String, String, String> urlCreator = (key, value) -> {
            Map<String, List<String>> thePerms = new HashMap<>(wrapper.getQueryPerms());
            thePerms.put(key, List.of(value));
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, List<String>> stringListEntry : thePerms.entrySet()) {
                builder.append(stringListEntry.getKey()).append("=").append(String.join(",", stringListEntry.getValue())).append("&");
            }
            return "hosts?" + builder.substring(0, builder.length() - 1);
        };

        for (Stat<?> stat : HostsPage.stats) {
            String name = StringUtils.capitalizeFirstLetter(stat.name().replace("(", "").replace(")", ""));
            sortMap.put(name, urlCreator.apply("sort", stat.name()));
        }


        wrapper.queryParam("sort")
                .ifPresent(s -> HostsPage.stats.stream()
                        .filter(stat -> StringUtils.capitalizeFirstLetter(stat.name().replace("(", "").replace(")", "")).equalsIgnoreCase(s))
                        .forEach(stat -> sort(cardStats, stat.name(), stat.comparator())));

        wrapper.html(MyWebUtils.makeCardPage("Stats", "", "Host", sortMap, cardStats));
    }


    public static List<Pair<String, String>> createFor(Staff staff, List<Match> matches, String requestID) {
        return HostsPage.stats.stream()
                .map(stat -> Pair.of(stat.name, StatTypes.getStat(stat.cachedStat, requestID, matches).toString())).collect(Collectors.toList());
    }

    public static void sort(List<Pair<String, List<Pair<String, String>>>> map, String key, Comparator<String> stringComparator) {
        map.sort((o1, o2) -> {
            String value = null;
            String value1 = null;
            for (Pair<String, String> stringStringPair : o2.value()) {
                if(stringStringPair.key().equals(key)) {
                    value = stringStringPair.value();
                }
            }
            for (Pair<String, String> stringStringPair : o1.value()) {
                if(stringStringPair.key().equals(key)) {
                    value1 = stringStringPair.value();
                }
            }
            return stringComparator.compare(value, value1);
        });
    }

    public record Stat<T>(String name, IStatType<T> cachedStat, Comparator<String> comparator) {}

    @Override
    public String getPath() {
        return "hosts";
    }



}
