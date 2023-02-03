package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DifferentHostInARow implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        List<List<Match>> differentHosts = new ArrayList<>();
        var ref = new Object() {
            List<Pair<Staff, Match>> staff = new ArrayList<>();
        };
        hostMatchMap.values().stream()
                .flatMap(List::stream)
                .filter(Match::isGoodGame)
                .filter(Match::isApolloGame)
                .sorted(Comparator.comparing(Match::opens))
                .forEach(match -> {
                    match.findStaff().ifPresent(staff1 -> {
                        for (Pair<Staff, Match> staff : ref.staff) {
                            if(staff.key().equals(staff1)) {
                                List<Match> matches = new ArrayList<>();
                                for (Pair<Staff, Match> staffMatchPair : ref.staff) {
                                    matches.add(staffMatchPair.value());
                                }
                                differentHosts.add(matches);
                                ref.staff = new ArrayList<>();
                            }
                        }
                        Pair<Staff, Match> staff = Pair.of(staff1, match);
                        ref.staff.add(staff);
                    });
                });

        int maxSizeOfList = 0;
        List<Match> maxList = new ArrayList<>();
        for (List<Match> matchList : differentHosts) {
            if(matchList.size() > maxSizeOfList) {
                maxSizeOfList = matchList.size();
                maxList = matchList;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Match match : maxList) {
            stringBuilder.append(match.getUrl()).append("\n");
        }
        wrapper.html(stringBuilder.toString());
    }

    @Override
    public String getPath() {
        return "different-host-in-a-row";
    }
}
