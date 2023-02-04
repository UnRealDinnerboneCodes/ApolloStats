package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.web.WebUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DifferentHostInARow implements IStatPage {

    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        List<List<Match>> differentHosts = new ArrayList<>();
        var ref = new Object() {
            List<Pair<Staff, Match>> staff = new ArrayList<>();
        };
        hostMatchMap.values().stream()
                .flatMap(List::stream)
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

        List<HostMatch> hostMatches = differentHosts.stream()
                .map(HostMatch::new)
                .sorted(Comparator.comparingInt(hostMatch -> hostMatch.matches().size()))
                .toList();
        wrapper.html(WebUtils.makeHTML("Different Host In A Row", "", List.of("Start", "End", "Count", "Matches"), hostMatches));
    }

    public record HostMatch(List<Match> matches) implements Supplier<List<String>> {
        @Override
        public List<String> get() {
            Instant start = Instant.parse(matches.get(0).opens());
            Instant end = Instant.parse(matches.get(matches.size() - 1).opens());
            String names = matches.stream()
                    .map(match -> WebUtils.formatAsClickableLink(match.getUrl(), match.getNumberedName()))
                    .collect(Collectors.joining(", "));
            return List.of(Util.formatData(start), Util.formatData(end), String.valueOf(matches.size()), names);
        }
    }

    @Override
    public String getPath() {
        return "different-host-in-a-row";
    }
}
