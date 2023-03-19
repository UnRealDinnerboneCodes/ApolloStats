package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GamesPage implements IStatPage {

    @Override
    public boolean filterMatches(Match match) {
        return match.isApolloGame();
    }

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, Integer> gamesHosted = new HashMap<>();
        Map<Staff, List<Integer>> gameNumbers = new HashMap<>();
        Map<Staff, List<Integer>> matchesRemoved = new HashMap<>();
        hostMatchMap.forEach((staff, matches) -> {
            int totalGamesHosted = 0;
            int totalMatchesRemoved = 0;
            List<Integer> theGameNumbers = new ArrayList<>();
            for (Match match : matches) {
                if(match.isGoodGame()) {
                    totalGamesHosted++;
                    theGameNumbers.add(match.count());
                }
                if(match.removed()) {
                    totalMatchesRemoved++;
                }
            }
            gamesHosted.put(staff, totalGamesHosted);
            gameNumbers.put(staff, theGameNumbers);
            matchesRemoved.put(staff, List.of(totalMatchesRemoved));
        });
        List<GameStats> gameStats = new ArrayList<>();
        for (Map.Entry<Staff, Integer> staffIntegerEntry : gamesHosted.entrySet()) {
            gameStats.add(new GameStats(staffIntegerEntry.getKey(), staffIntegerEntry.getValue(), gameNumbers.get(staffIntegerEntry.getKey()), matchesRemoved.get(staffIntegerEntry.getKey())));
        }
        wrapper.html(WebUtils.makeHtmlTable("Host Matches", "", Arrays.asList("Host", "Games", "Height Game", "Missing Games", "Duplicate Games"), gameStats));

    }

    public record GameStats(Staff staff, int gamesHosted, List<Integer> gameNumbers, List<Integer> matchesRemoved) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            int heightGame = gameNumbers.stream().mapToInt(Integer::intValue).max().orElse(0);
            int lowestGame = gameNumbers.stream().mapToInt(Integer::intValue).min().orElse(0);
            List<Integer> missingGames = IntStream.range(lowestGame, heightGame).filter(i -> !gameNumbers.contains(i)).boxed().toList();
            List<Integer> duplicateGames = gameNumbers.stream().filter(i -> gameNumbers.indexOf(i) != gameNumbers.lastIndexOf(i)).toList();
            Set<Integer> uniqueGames = new LinkedHashSet<>(duplicateGames);
            String missingGamesString =  missingGames.stream().map(i -> "#" + i).collect(Collectors.joining(", "));
            String duplicateGamesString = uniqueGames.stream().map(i -> "#" + i).collect(Collectors.joining(", "));
            return List.of(staff.displayName(), String.valueOf(gamesHosted), String.valueOf(heightGame), missingGamesString, duplicateGamesString);
        }
    }

    @Override
    public String getPath() {
        return "matches";
    }
}
