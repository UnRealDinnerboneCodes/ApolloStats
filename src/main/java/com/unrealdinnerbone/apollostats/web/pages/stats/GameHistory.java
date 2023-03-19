package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GameHistory implements IStatPage {

    private static final Game UNKNOWN = new Game(0, 0);

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        List<Match> matches = hostMatchMap.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Match::getOpenTime).reversed())
                .toList();
        List<GameStats> gameStats = new ArrayList<>();
        for(Match match : matches) {
            Game game = Stats.INSTANCE.getGameManager().findGame(match.id()).orElse(UNKNOWN);
            gameStats.add(new GameStats(match.hostingName(), Instant.parse(match.opens()), Stats.INSTANCE.getScenarioManager().fix(Type.SCENARIO, match.scenarios()), game.fill()));
        }
        wrapper.html(WebUtils.makeHtmlTable("Match History", "", Arrays.asList("Host", "Time", "Scens", "Fill"), gameStats));
    }

    @Override
    public String getPath() {
        return "history";
    }

    public record GameStats(String host, Instant time, List<Scenario> scens, int fill) implements Supplier<List<String>> {


        private final static DecimalFormat df = new DecimalFormat("#.##");



        @Override
        public List<String> get() {
            return Arrays.asList(host, Util.formatData(time), scens.stream().map(Scenario::name).collect(Collectors.joining(", ")), df.format(fill));
        }
    }
}
