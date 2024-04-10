package com.unrealdinnerbone.apollostats.api;

import java.util.Arrays;
import java.util.function.Predicate;

public enum GameState implements Predicate<String> {
    IDLE(s -> s.equalsIgnoreCase("Apollo » No game is running.\nWhitelist is on.")),
    LOBBY(s -> s.startsWith("Apollo » No game is running.") && s.contains("Arena is")),
    PRE_PVP(s -> s.startsWith("Apollo » PvP is in: ")),
    PVP(s -> s.startsWith("Apollo » Meetup is in: ")),
    MEATUP(s -> s.startsWith("Apollo » Meetup is now!")),
    //        OVER(s -> false),
    UNKNOWN(s -> false),
    ;

    private static final GameState[] VALUES = values();

    private final Predicate<String> matches;

    GameState(Predicate<String> matches) {
        this.matches = matches;
    }


    public static GameState getState(String name) {
        return Arrays.stream(VALUES).filter(state -> state.test(name)).findFirst().orElse(UNKNOWN);
    }

    @Override
    public boolean test(String s) {
        return matches.test(s);
    }
}
