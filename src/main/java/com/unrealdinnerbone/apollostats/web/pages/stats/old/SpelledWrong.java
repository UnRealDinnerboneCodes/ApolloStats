package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.Scenarios;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SpelledWrong implements IWebPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpelledWrong.class);
    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
        List<Spelling> spellings = new ArrayList<>();
        for(Map.Entry<String, List<Match>> stringListEntry : hostMatchMap.entrySet()) {
            AtomicInteger total = new AtomicInteger();
            Stream<List<String>> values = stringListEntry.getValue().stream()
                    .filter(Match::isApolloGame)
                    .filter(match -> Instant.now().isAfter(Instant.parse(match.opens())))
                    .map(Match::scenarios);

            values.forEach(scenarios -> {
                scenarios.forEach(scenario -> {
                    List<String> cake = Scenarios.fix(Type.SCENARIO, List.of(scenario)).stream().map(Scenario::name).toList();

                    if(cake.size() > 0) {
                        boolean found = false;
                        for(String s : cake) {
                            if(s.equalsIgnoreCase(scenario)) {
                                found = true;
                                break;
                            }
                        }
                        if(!found) {
                            if(!cake.contains("Hasty Boys")) {
                                LOGGER.info("{} was spelled {}", scenario, cake);
                                total.getAndIncrement();
                            }

                        }else {
                            found=found;
                        }
                    }else {
                        cake.size();
                    }
                });
            });
            spellings.add(new Spelling(stringListEntry.getKey(), total.get()));
        }

        return WebUtils.makeHTML("Scens Spelled Wrong", "https://unreal.codes/kevStonk.png", Arrays.asList("Host", "Wrong"), spellings);
    }

    public record Spelling(String name, int count) implements Supplier<List<String>> {

        @Override
        public List<String> get() {
            return Arrays.asList(name, String.valueOf(count));
        }
    }

    @Override
    public String getName() {
        return "spelled";
    }
}
