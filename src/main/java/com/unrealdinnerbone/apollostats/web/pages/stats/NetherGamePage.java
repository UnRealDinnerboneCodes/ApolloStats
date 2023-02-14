package com.unrealdinnerbone.apollostats.web.pages.stats;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.web.WebUtils;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class NetherGamePage implements IStatPage {

    private static final Logger LOGGER = LogHelper.getLogger();

    private static final List<String> ON_TYPE = List.of("enabled", "enable", "on", "eeabled");
    private static final List<String> OF_TYPE = List.of("disabled", "disable", "off", "discabled");
    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        Map<Staff, AtomicInteger> netherOn = new HashMap<>();
        Map<Staff, AtomicInteger> netherOff = new HashMap<>();
        Map<Staff, AtomicInteger> unknown = new HashMap<>();
        hostMatchMap.forEach((staff, matches) -> {
            AtomicInteger on = new AtomicInteger();
            AtomicInteger off = new AtomicInteger();
            matches.forEach(match -> {
                String netherType = match.isNether();
                String type = match.isNether().toLowerCase();
                if(ON_TYPE.contains(type)) {
                    on.incrementAndGet();
                } else if(OF_TYPE.contains(type)) {
                    off.incrementAndGet();
                } else {
                    unknown.computeIfAbsent(staff, staff1 -> new AtomicInteger()).incrementAndGet();
                }
            });
            netherOn.put(staff, on);
            netherOff.put(staff, off);
        });
        List<NetherGameStats> netherGameStats = new ArrayList<>();
        netherOn.forEach((staff, integer) -> netherGameStats.add(new NetherGameStats(staff, integer.get(), netherOff.get(staff).get(), unknown.getOrDefault(staff, new AtomicInteger()).get())));
        wrapper.html(WebUtils.makeHTML("Nether Games", "", Arrays.asList("Host", "On", "Off", "Unknow", "% On"), netherGameStats));

    }


    public record NetherGameStats(Staff staff, int on, int off, int unknown) implements Supplier<List<String>> {

        private final static DecimalFormat df = new DecimalFormat("#.##");
        @Override
        public List<String> get() {
            String percentageOn = df.format((double) on / (on + off + unknown) * 100);
            return List.of(staff.displayName(), String.valueOf(on), String.valueOf(off), String.valueOf(unknown), percentageOn);
        }
    }

    @Override
    public String getPath() {
        return "nether";
    }
}
