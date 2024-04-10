package com.unrealdinnerbone.apollo.core.stats.types;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record TimeBetweenStat(Instant from, Instant to) {

    public long getBetween(ChronoUnit chronoUnit) {
        return chronoUnit.between(from, to);
    }

}
