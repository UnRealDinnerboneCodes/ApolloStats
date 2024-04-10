package com.unrealdinnerbone.apollo.core.api;

import java.util.Arrays;

public enum Type {
    TEAM,
    SCENARIO,
    MYSTERY_SCENARIO;

    public static Type fromString(String s) {
        return Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(s)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown type: " + s));
    }
}
