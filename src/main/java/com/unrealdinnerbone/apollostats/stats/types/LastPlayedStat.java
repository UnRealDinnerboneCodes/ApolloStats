package com.unrealdinnerbone.apollostats.stats.types;

import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.TimeUtil;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public record LastPlayedStat(String name, Pair<Instant, String> first, Pair<Instant, String> last, int timesPlayed, double percent) implements Supplier<List<String>> {

    private final static DecimalFormat df = new DecimalFormat("#.##");

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd")
            .withLocale(Locale.UK)
            .withZone(ZoneId.of("UTC"));

    @Override
    public List<String> get() {
        long between = ChronoUnit.DAYS.between( last.key(), TimeUtil.utcNow());
        return Arrays.asList(name, formatter.format(first.key()), formatter.format(last.key()), first.value(), last.value(), "Days: " + String.format("%03d", between), String.valueOf(timesPlayed), df.format(percent * 100) + "%");
    }
}
