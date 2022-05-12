package com.unrealdinnerbone.apollostats.api;

public record Scenario(String name, int id, boolean image, boolean official, Type type) {
    @Override
    public String toString() {
        return name();
    }

}
