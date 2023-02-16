package com.unrealdinnerbone.apollostats.api;

import java.util.List;

public record Scenario(String name, int id, boolean image, boolean official, Type type, List<Integer> required, List<Integer> disallowed) {
    @Override
    public String toString() {
        return name();
    }

    public boolean isRequired(Scenario scenario) {
        return required.contains(scenario.id());
    }

    public boolean isDisallowed(Scenario scenario) {
        return disallowed.contains(scenario.id());
    }

}
