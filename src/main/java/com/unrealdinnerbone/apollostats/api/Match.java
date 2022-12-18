package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.GameManager;
import com.unrealdinnerbone.apollostats.mangers.StaffManager;
import com.unrealdinnerbone.unreallib.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
                    String content,
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
        return Util.utcNow().isAfter(Instant.parse(opens()));
    }

    public boolean hasPlayed() {
        return Util.utcNow().minus(1, ChronoUnit.HOURS).isAfter(Instant.parse(opens()));
    }

    @Override
    public int hashCode() {
        return id();
    }

    @Override
    public String toString() {
        return displayName() + " (" + id + ")";
    }

    public Optional<Game> findGameData() {
        return GameManager.getGames().stream().filter(game -> game.id() == id).findFirst();
    }

    public Optional<Staff> findStaff() {
        return StaffManager.getStaff().stream().filter(staff -> staff.username().equals(author)).findFirst();
    }

    public String getUrl() {
        return Stats.CONFIG.getMatchPage() + id;
    }

    public String displayName() {
        return hostingName() != null ? hostingName() : author();
    }

    public Optional<String> getStaffDisplayName() {
        return findStaff().map(Staff::displayName);
    }

    public String isNether() {
        for(String s : content.split("\n")) {
            if(s.startsWith("**Nether**")) {
                String[] split = s.split("|");
                boolean enabled = split.length == 2 && split[1].replace(" ", "").equals("true");
                return enabled ? "Enabled" : "Disabled";
            }
        }
        return "Unknown";
    }

    public String getTeamFormat() {
        if(teams().equalsIgnoreCase("FFA")) {
            return "FFA";
        }else {
            String value = StringUtils.capitalizeFirstLetter(teams().equalsIgnoreCase("custom") ? customStyle().toLowerCase() : teams().toLowerCase());
            if(size != null && size > 1) {
                value += " (" + size + ")";
            }
            return value;
        }
    }
}
