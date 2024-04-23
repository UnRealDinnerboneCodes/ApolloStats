package com.unrealdinnerbone.apollo.core.api;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Staff(String username, @Nullable Instant staffDate, String displayName, Type type) {


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
    public static final Staff APOLLO = new Staff("Apollo", null,"Apollo", Type.OWNER);
\
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
        RETIRED;


        public static Optional<Type> fromString(String string) {
            return Arrays.stream(values())
                    .filter(type -> type.name().equalsIgnoreCase(string)).findFirst();
        }
    }

}
