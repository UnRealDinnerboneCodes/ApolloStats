package com.unrealdinnerbone.apollostats.api;

public record Game(int id, int fill)
{
    @Override
    public int hashCode() {
        return id;
    }
}
