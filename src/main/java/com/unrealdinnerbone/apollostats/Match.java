package com.unrealdinnerbone.apollostats;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class Match {

    private static final List<String> adders = Arrays.asList("na.apollouhc.com", "apollouhc.net", "apollouhc.com");
    private final int id;
    private final String author;
    private final String opens;
    private final String address;
    private final String ip;
    private final List<String> scenarios;
    private final List<String> tags;
    private final String teams;
    private final Integer size;
    private final String customStyle;
    private final int count;
    private final String region;
    private final boolean removed;
    private final String removedBy;
    private final String removedReason;
    private final String created;
    private final List<String> roles;
    private final String location;
    private final String mainVersion;
    private final String version;
    private final int slots;
    private final int length;
    private final int mapSize;
    private final int pvpEnabledAt;
    private final String approvedBy;
    private final String hostingName;
    private final boolean tournament;

    public Match(int id,
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
        this.id = id;
        this.author = author;
        this.opens = opens;
        this.address = address;
        this.ip = ip;
        this.scenarios = scenarios;
        this.tags = tags;
        this.teams = teams;
        this.size = size;
        this.customStyle = customStyle;
        this.count = count;
        this.region = region;
        this.removed = removed;
        this.removedBy = removedBy;
        this.removedReason = removedReason;
        this.created = created;
        this.roles = roles;
        this.location = location;
        this.mainVersion = mainVersion;
        this.version = version;
        this.slots = slots;
        this.length = length;
        this.mapSize = mapSize;
        this.pvpEnabledAt = pvpEnabledAt;
        this.approvedBy = approvedBy;
        this.hostingName = hostingName;
        this.tournament = tournament;
    }

    public boolean isApolloGame() {
        return adders.contains(address().toLowerCase(Locale.ROOT)) || tags().stream().map(String::toLowerCase).toList().contains("apollo");
    }

    public int id() {
        return id;
    }

    public String author() {
        return author;
    }

    public String opens() {
        return opens;
    }

    public String address() {
        return address;
    }

    public String ip() {
        return ip;
    }

    public List<String> scenarios() {
        return scenarios;
    }

    public List<String> tags() {
        return tags;
    }

    public String teams() {
        return teams;
    }

    public Integer size() {
        return size;
    }

    public String customStyle() {
        return customStyle;
    }

    public int count() {
        return count;
    }

    public String region() {
        return region;
    }

    public boolean removed() {
        return removed;
    }

    public String removedBy() {
        return removedBy;
    }

    public String removedReason() {
        return removedReason;
    }

    public String created() {
        return created;
    }

    public List<String> roles() {
        return roles;
    }

    public String location() {
        return location;
    }

    public String mainVersion() {
        return mainVersion;
    }

    public String version() {
        return version;
    }

    public int slots() {
        return slots;
    }

    public int length() {
        return length;
    }

    public int mapSize() {
        return mapSize;
    }

    public int pvpEnabledAt() {
        return pvpEnabledAt;
    }

    public String approvedBy() {
        return approvedBy;
    }

    public String hostingName() {
        return hostingName;
    }

    public boolean tournament() {
        return tournament;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Match) obj;
        return this.id == that.id &&
                Objects.equals(this.author, that.author) &&
                Objects.equals(this.opens, that.opens) &&
                Objects.equals(this.address, that.address) &&
                Objects.equals(this.ip, that.ip) &&
                Objects.equals(this.scenarios, that.scenarios) &&
                Objects.equals(this.tags, that.tags) &&
                Objects.equals(this.teams, that.teams) &&
                Objects.equals(this.size, that.size) &&
                Objects.equals(this.customStyle, that.customStyle) &&
                this.count == that.count &&
                Objects.equals(this.region, that.region) &&
                this.removed == that.removed &&
                Objects.equals(this.removedBy, that.removedBy) &&
                Objects.equals(this.removedReason, that.removedReason) &&
                Objects.equals(this.created, that.created) &&
                Objects.equals(this.roles, that.roles) &&
                Objects.equals(this.location, that.location) &&
                Objects.equals(this.mainVersion, that.mainVersion) &&
                Objects.equals(this.version, that.version) &&
                this.slots == that.slots &&
                this.length == that.length &&
                this.mapSize == that.mapSize &&
                this.pvpEnabledAt == that.pvpEnabledAt &&
                Objects.equals(this.approvedBy, that.approvedBy) &&
                Objects.equals(this.hostingName, that.hostingName) &&
                this.tournament == that.tournament;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, author, opens, address, ip, scenarios, tags, teams, size, customStyle, count, region, removed, removedBy, removedReason, created, roles, location, mainVersion, version, slots, length, mapSize, pvpEnabledAt, approvedBy, hostingName, tournament);
    }

    @Override
    public String toString() {
        return "Match[" +
                "id=" + id + ", " +
                "author=" + author + ", " +
                "opens=" + opens + ", " +
                "address=" + address + ", " +
                "ip=" + ip + ", " +
                "scenarios=" + scenarios + ", " +
                "tags=" + tags + ", " +
                "teams=" + teams + ", " +
                "size=" + size + ", " +
                "customStyle=" + customStyle + ", " +
                "count=" + count + ", " +
                "region=" + region + ", " +
                "removed=" + removed + ", " +
                "removedBy=" + removedBy + ", " +
                "removedReason=" + removedReason + ", " +
                "created=" + created + ", " +
                "roles=" + roles + ", " +
                "location=" + location + ", " +
                "mainVersion=" + mainVersion + ", " +
                "version=" + version + ", " +
                "slots=" + slots + ", " +
                "length=" + length + ", " +
                "mapSize=" + mapSize + ", " +
                "pvpEnabledAt=" + pvpEnabledAt + ", " +
                "approvedBy=" + approvedBy + ", " +
                "hostingName=" + hostingName + ", " +
                "tournament=" + tournament + ']';
    }

}
