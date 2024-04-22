package com.unrealdinnerbone.apollo.core.api;

public record Game(int id, int fill) {
    @Override
    public int hashCode() {
        return id;
    }
}
