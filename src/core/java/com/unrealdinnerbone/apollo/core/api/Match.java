package com.unrealdinnerbone.apollo.core.api;


import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.unreallib.StringUtils;
import com.unrealdinnerbone.unreallib.TimeUtil;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        return TimeUtil.utcNow().isAfter(getOpenTime());
    }

    public Instant getOpenTime() {
        return Instant.parse(opens());
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
        return Stats.INSTANCE.getGameManager().getGames().stream().filter(game -> game.id() == id).findFirst();
    }

    public Optional<Staff> findStaff() {
        return Stats.INSTANCE.getStaffManager().getStaff().stream().filter(staff -> staff.username().equals(author)).findFirst();
    }

    public String getUrl() {
        return Stats.INSTANCE.getStatsConfig().getMatchPage() + id;
    }

    public String displayName() {
        return hostingName() != null ? hostingName() : author();
    }

    public Optional<String> getStaffDisplayName() {
        return findStaff().map(Staff::displayName);
    }

    public String getNumberedName() {
        return getStaffDisplayName().orElse(displayName()) + "#" + id;
    }

    public static final List<String> ON_TYPE = List.of("enabled", "enable", "on", "eeabled");
    public String getNetherFormat() {
        for(String s : content.split("\n")) {
            if(s.startsWith("**Nether**") || s.startsWith("Nether")) {
                String[] split = s.split("\\|");
                if(split.length == 2) {
                    return split[1].replace(" ", "");
                }else {
                    return "Unknown";
                }
            }
        }
        return "Unknown";
    }

    public boolean isNetherEnabled() {
        return ON_TYPE.contains(getNetherFormat().toLowerCase());
    }

    public String getTeamFormat() {
        return teams().equalsIgnoreCase("FFA") ? "FFA" : toSpacedCamelCase(teams().equalsIgnoreCase("custom") ? customStyle().toLowerCase() : teams().toLowerCase());
    }

    public static String toSpacedCamelCase(String s){
        return Arrays.stream(s.split(" "))
                .map(StringUtils::capitalizeFirstLetter)
                .collect(Collectors.joining(" "));
    }

    private static final Map<String, Integer> amount = new HashMap<>();

    static {
        IntStream.range(0, 10).forEach(i -> amount.put("to" + i, i));
    }

    public int getTeamSize() {
        if (size != null) {
            return size;
        } else if (teams().equalsIgnoreCase("FFA")) {
            return 1;
        }else {
            String teamFormat = getTeamFormat().toLowerCase();
            for (Map.Entry<String, Integer> stringIntegerEntry : amount.entrySet()) {
                String key = stringIntegerEntry.getKey();
                if (teamFormat.contains(key)) {
                    return stringIntegerEntry.getValue();
                }
            }
            return 0;
        }
    }

    private static final Map<String, Integer> teamAmount = new HashMap<>();

    static {
        teamAmount.put("red vs blue vs green vs yellow vs purple", 5);
        teamAmount.put("rvbvgvyvp", 5);
        teamAmount.put("red v. blue v. green v. yellow", 4);
        teamAmount.put("red vs. blue vs. green vs. yellow", 4);
        teamAmount.put("red v. blue v. yellow v. green", 4);
        teamAmount.put("red v. blue v. yellow v. green v. purple", 4);
        teamAmount.put("rbgy", 4);
        teamAmount.put("rvbvgvy", 4);
        teamAmount.put("red v blue v green v yellow", 4);
        teamAmount.put("red vs blue vs green vs yellow", 4);
        teamAmount.put("red vs blue vs green", 3);
        teamAmount.put("red vs green vs white", 3);
        teamAmount.put("blue vs. green vs. yellow", 3);
        teamAmount.put("red v. blue v. green", 3);
        teamAmount.put("rvb", 2);
        teamAmount.put("red vs blue", 2);
        teamAmount.put("red v blue", 2);
        teamAmount.put("red vs. blue", 2);
        teamAmount.put("red v. blue", 2);
    }

    public int getTeamAmount() {
        String s = getTeamFormat().toLowerCase();
        return teamAmount.entrySet().stream().filter(stringIntegerEntry -> s.contains(stringIntegerEntry.getKey())).findFirst().map(Map.Entry::getValue).orElse(0);
    }


    public int getSeason() {
        //1.19.2
        if(id >= 124589) {
            return 12;
        }
        //1.19
        if(id >= 119177) {
            return 11;
        }
        //1.18
        if(id >= 111637) {
            return 10;
        }
        //1.17
        if(id >= 107657) {
            return 9;
        }
        //1.17
        if(id >= 102569) {
            return 8;
        }
        //1.16.4
        if(id >= 94185) {
            return 7;
        }
        //1.16.1
        if(id >= 82936) {
            return 6;
        }
        //1.15.2
        if(id >= 64790) {
            return 5;
        }
        //1.14.4
        if(id >= 56048) {
            return 4;
        }
        //1.14.4
        if(id >= 47164) {
            return 3;
        }
        return 0;
    }

    public boolean isRush() {
        return length < 45;
    }
}
