package com.unrealdinnerbone.apollostats.web.pages.stats.old;

import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.Match;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Season9Thing implements IWebPage {

    @Override
    public String generateStats(Map<String, List<Match>> hostMatchMap) {
       return String.valueOf(hostMatchMap.values().stream().flatMap(Collection::stream)
               .filter(Match::isApolloGame)
               .filter(Predicate.not(Match::removed))
               .filter(match -> {
                   return match.id() >= 111637;
               }).count());
    }

    @Override
    public String getName() {
        return "asd";
    }


    public record Count(String scenario, int amount, double percent) implements Supplier<List<String>> {

        private final static DecimalFormat df = new DecimalFormat("#.#####");

        @Override
        public List<String> get() {
            return Arrays.asList(scenario, String.valueOf(amount), df.format(percent) + "%");
        }
    }

}
