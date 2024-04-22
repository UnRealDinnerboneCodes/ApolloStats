package com.unrealdinnerbone.apollo.core.api;

import java.util.List;
import java.util.Objects;

public record Scenario(String name,
                       int id,
                       boolean image,
                       boolean official,
                       Type type,
                       List<Integer> required,
                       List<Integer> disallowed,
                       boolean hostable,
                       boolean meta) {

    @Override
    public String toString() {
        return name();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean isSame(int id) {
       return this.id() == id;
    }

    public boolean isRequired(Scenario scenario) {
        return required.contains(scenario.id());
    }

    public boolean isDisallowed(Scenario scenario) {
        return disallowed.contains(scenario.id());
    }

}
