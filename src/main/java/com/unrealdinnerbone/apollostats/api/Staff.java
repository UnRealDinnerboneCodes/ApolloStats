package com.unrealdinnerbone.apollostats.api;

import java.util.Objects;

public record Staff(String username, String displayName) {

    public static final Staff UNKNOWN = new Staff("Unknown", "Unknown");
    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
