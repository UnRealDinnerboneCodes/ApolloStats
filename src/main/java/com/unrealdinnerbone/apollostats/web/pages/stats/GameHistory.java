package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.mangers.GameManager;
import com.unrealdinnerbone.apollostats.mangers.ScenarioManager;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GameHistory implements IStatPage {

    private static final Game UNKNOWN = new Game(0, 0);

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        List<Match> matches = hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isGoodGame)
                .filter(Match::isApolloGame)
                .toList();
        List<GameStats> gameStats = new ArrayList<>();
        for(Match match : matches) {
            Game game = GameManager.findGame(match.id()).orElse(UNKNOWN);
            gameStats.add(new GameStats(match.hostingName(), Instant.parse(match.opens()), ScenarioManager.fix(Type.SCENARIO, match.scenarios()), game.fill()));
        }
        wrapper.html(WebUtils.makeHTML("Match History", "", Arrays.asList("Host", "Time", "Scens", "Fill"), gameStats));
    }

    @Override
    public String getPath() {
        return "games";
    }

    public record GameStats(String host, Instant time, List<Scenario> scens, int fill) implements Supplier<List<String>> {


        private final static DecimalFormat df = new DecimalFormat("#.##");

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
                .withLocale(Locale.UK)
                .withZone(ZoneId.of("UTC"));

        @Override
        public List<String> get() {
            return Arrays.asList(host, formatter.format(time), scens.stream().map(Scenario::name).collect(Collectors.joining(", ")), df.format(fill));
        }
    }
}
