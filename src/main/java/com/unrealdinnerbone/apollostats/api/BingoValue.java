package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.Util;

public record BingoValue(String bingo, boolean isBingo, boolean isPlayer) {
    @Override
    public int hashCode() {
        return Util.formalize(bingo).toLowerCase().hashCode();
    }
}
