package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.Games;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public record Match(int id,
                    String author,
                    String opens,
                    String address,
                    String ip,
                    List<String> scenarios,
                    List<String> tags,
                    String teams,
                    Integer size,
                    String customStyle,
                    int count,
//                    String content,
                    String region,
                    boolean removed,
                    String removedBy,
                    String removedReason,
                    String created,
                    List<String> roles,
                    String location,
                    String mainVersion,
                    String version,
                    int slots,
                    int length,
                    int mapSize,
                    int pvpEnabledAt,
                    String approvedBy,
                    String hostingName,
                    boolean tournament) {

    private static final List<String> adders = Arrays.asList("na.apollouhc.com", "apollouhc.net", "apollouhc.com");

    public boolean isApolloGame() {
        return adders.contains(address().toLowerCase(Locale.ROOT)) || tags().stream().map(String::toLowerCase).toList().contains("apollo");
    }

    public boolean isGoodGame() {
        return !removed() && isApolloGame() && hasHappened();
    }

    public boolean hasHappened() {
        return Instant.now().isAfter(Instant.parse(opens()));
    }

    public boolean hasPlayed() {
        return Instant.now().minus(1, ChronoUnit.HOURS).isAfter(Instant.parse(opens()));
    }

    @Override
    public int hashCode() {
        return id();
    }

    public Optional<Game> findGameData() {
        return Games.getGames().stream().filter(game -> game.id() == id).findFirst();
    }

    public String displayName() {
        return hostingName() != null ? hostingName() : author();
    }
}
