package com.unrealdinnerbone.apollostats.api;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Staff(String username, String displayName, Type type) {


    public Staff {
        if (username == null) {
            throw new IllegalArgumentException("Username can not be null");
        }
        if (displayName == null) {
            throw new IllegalArgumentException("Display Name can not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type can not be null");
        }
    }

    public static final Staff UNKNOWN = new Staff("Unknown", "Unknown", Type.OTHER);
    public static final Staff APOLLO = new Staff("Apollo", "Apollo", Type.OWNER);
    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public String toString() {
        return displayName;
    }


    public enum Type {
        OWNER,
        SENIOR,
        STAFF,
        TRAIL,
        RETIRED,

        OTHER;
        ;

        private static final List<Type> TYPES = Arrays.stream(values()).filter(type -> type != OTHER).toList();

        public static Optional<Type> fromString(String string) {
            return TYPES.stream()
                    .filter(type -> type.name().equalsIgnoreCase(string)).findFirst();
        }
    }

}
