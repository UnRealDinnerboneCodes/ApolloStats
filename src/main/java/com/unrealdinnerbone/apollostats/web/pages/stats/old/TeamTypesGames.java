package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class TeamTypesGames implements IStatPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamTypesGames.class);

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, Map<String, AtomicInteger>> types = new HashMap<>();
        List<String> typesList = new ArrayList<>();
        typesList.add("Host");


        hostMatchMap.forEach((host, matches) -> {
            Map<String, AtomicInteger> map = new HashMap<>();
            for(Match match : matches) {
                if(match.isApolloGame() && !match.removed()) {
                    Maps.putIfAbsent(map, match.teams(), new AtomicInteger(0)).incrementAndGet();
                    if(!typesList.contains(match.teams())) {
                        typesList.add(match.teams());
                    }

                }
            }
            types.put(host, map);
        });


        List<Supplier<List<String>>> iTableData = new ArrayList<>();

        for(Map.Entry<Staff, Map<String, AtomicInteger>> entry : types.entrySet()) {
            iTableData.add(() -> {
                List<String> values = new ArrayList<>();
                values.add(entry.getKey().displayName());
                for(String s : typesList) {
                    if(!s.equals("Host")) {
                        values.add(String.valueOf(entry.getValue().getOrDefault(s, new AtomicInteger(0)).get()));
                    }
                }
                return values;
            });
        }
        wrapper.html(WebUtils.makeHTML("Team Types", "", typesList, iTableData));
    }

    @Override
    public String getPath() {
        return "team_types";
    }

    public record Stats(String name, Pair<Instant, String> first, Pair<Instant, String> last) implements Supplier<List<String>> {

        @Override
        public List<String> get() {
            return Arrays.asList(name, first.key().toString(), last.key().toString(), first.value(), last.value());
        }
    }
}
