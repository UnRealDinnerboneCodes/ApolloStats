package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.lib.Util;

public record BingoValue(String bingo, boolean isBingo, boolean isPlayer) {
    @Override
    public int hashCode() {
        return Util.formalize(bingo).toLowerCase().hashCode();
    }
}
