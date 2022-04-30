package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.unreallib.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Scenarios
{
    private static final String SCENARIOS_URL = System.getenv().getOrDefault("SCENARIOS_URL", "https://pastebin.com/raw/PixjeKaS");
    private static final Logger LOGGER = LoggerFactory.getLogger(Scenarios.class);
    private static final Map<Type, List<Scenario>> values = new HashMap<>();
    private static final Map<String, String> remaps = new HashMap<>();

    public static void loadDiskData() throws Exception {
        LOGGER.info("Loading disk data...");
        values.clear();
        Arrays.stream(Type.values()).forEach(value -> values.put(value, new ArrayList<>()));
        remaps.clear();

        Thing theMap = JsonUtil.DEFAULT.parse(Thing.class, Stats.getResourceAsString("scen.json"));
        for(Map.Entry<String, MapObject> stringMapObjectEntry : theMap.data().entrySet()) {
            for(String value : stringMapObjectEntry.getValue().values()) {
                remaps.put(value, stringMapObjectEntry.getKey());
            }
            Type type = Type.fromString(stringMapObjectEntry.getValue().type());
            values.get(type).add(new Scenario(stringMapObjectEntry.getKey(), stringMapObjectEntry.getValue().id()));

        }

        LOGGER.info("Loaded {} remaps", remaps.size());

    }

    public record Thing(Map<String, MapObject> data) {}

    public record MapObject(String type, List<String> values, int id) {}

    public record Scenario(String name, int id) {}

    public enum Type {
        TEAM,
        SCENARIO,
        MYSTERY_SCENARIO
        ;

        public static Type fromString(String s) {
            return Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(s)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown type: " + s));
        }
    }

    public static List<String> fix(Type type, List<String> fixed) {
        List<String> cake = values.get(type).stream().map(Scenario::name).flatMap(s -> remap(s).stream()).toList();
        return fixed.stream()
                .flatMap(scenarioName -> remap(scenarioName).stream())
                .filter(scenario -> {

                    return cake.contains(scenario);
                })
                .collect(Collectors.toList());

    }

    public static List<String> remap(String scenario) {
        return remaps.entrySet().stream().filter(stringStringEntry -> scenario.equalsIgnoreCase(stringStringEntry.getKey())).map(Map.Entry::getValue).toList();
    }

    public static List<Scenario> getValues(Type type) {
        return values.get(type);
    }
}
