package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.LazyHashMap;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.Maps;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScenarioManager
{
    private static final Logger LOGGER = LogHelper.getLogger();

    private static final LevenshteinDistance LEVENSHTEIN_DISTANCE = LevenshteinDistance.getDefaultInstance();
    private static final Map<Type, List<Scenario>> values = new HashMap<>();
    private static final Map<String, List<Scenario>> remap = new HashMap<>();
    private static final LazyHashMap<String, List<Scenario>> MAP = new LazyHashMap<>(cache -> {
        if(remap.containsKey(cache)) {
            return remap.get(cache);
        }else {
            Map<Integer, List<Scenario>> values = new HashMap<>();
            for(Scenario scenario : getAll()) {
                if(isSimilar(scenario.name(), cache)) {
                    return List.of(scenario);
                }else {
                    Maps.putIfAbsent(values, LEVENSHTEIN_DISTANCE.apply(Util.formalize(scenario.name()), Util.formalize(cache)), new ArrayList<>()).add(scenario);
                }
            }
            List<Scenario> scenarios = values.keySet()
                    .stream()
                    .min(Integer::compareTo)
                    .map(values::get)
                    .stream()
                    .flatMap(Collection::stream)
                    .toList();
            if(scenarios.size() > 0) {
                LOGGER.info("Using Levenshtein Distance for {} -> {} [{}]", cache, scenarios, scenarios.stream().map(Scenario::id).collect(Collectors.toList()));
            }else {
                LOGGER.info("No Scenario found for {}", cache);
            }
            AlertManager.unknownSceneFound(cache, scenarios);
            return scenarios;
        }
    });



    public static boolean isSimilar(String name, String other) {
        return Util.formalize(name).equalsIgnoreCase(Util.formalize(other));
    }

    public static CompletableFuture<Void> init() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        TaskScheduler.scheduleRepeatingTaskExpectantly(1, TimeUnit.HOURS, task -> {
            ScenarioManager.loadScenData();
            ScenarioManager.loadRemapData();
            if(!future.isDone()) {
                future.complete(null);
            }
        }, e -> LOGGER.error("Failed to load scenarios", e));
        return future;
    }

    public static void loadScenData() throws SQLException {
        values.clear();
        Arrays.stream(Type.values()).forEach(value -> values.put(value, new ArrayList<>()));
        ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.scenario");
        while(resultSet.next()) {
            String name = resultSet.getString("name");
            String type = resultSet.getString("type");
            int id = resultSet.getInt("id");
            boolean isActive = resultSet.getBoolean("image");
            boolean official = resultSet.getBoolean("official");
            Integer[] required = (Integer[]) resultSet.getArray("required").getArray();
            Integer[] disallowed = (Integer[]) resultSet.getArray("disallowed").getArray();
            Type type1 = Type.fromString(type);
            values.get(type1).add(new Scenario(name, id, isActive, official, type1, Arrays.asList(required), Arrays.asList(disallowed)));
        }

    }

    public static void loadRemapData() throws SQLException {
        remap.clear();
        ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.scen_remap");
        while(resultSet.next()) {
            int scen = resultSet.getInt("scenId");
            String remap = resultSet.getString("name");
            find(scen).ifPresentOrElse(scenario -> {
                        Maps.putIfAbsent(ScenarioManager.remap, remap, new ArrayList<>());
                        if(!ScenarioManager.remap.get(remap).contains(scenario)) {
                            ScenarioManager.remap.get(remap).add(scenario);
                        }
                    },
                    () -> LOGGER.error("Failed to find scenario with id {} for remap {}", scen, remap));
        }

    }


    public static List<Scenario> fix(Type type, List<String> fixed) {
        List<Scenario> cake = values.get(type)
                .stream()
                .map(Scenario::name)
                .map(ScenarioManager::remap)
                .flatMap(Collection::stream)
                .toList();
        return fixed.stream()
                .map(ScenarioManager::remap)
                .flatMap(Collection::stream)
                .filter(scenario -> cake.stream().anyMatch(scenario1 -> scenario1.name().equalsIgnoreCase(scenario.name())))
                .filter(scenario -> scenario.type() == type)
                .collect(Collectors.toList());

    }

    public static Optional<Scenario> find(int id) {
        return values.values().stream().flatMap(Collection::stream).filter(scenario -> scenario.id() == id).findFirst();
    }

    private static List<Scenario> remap(String scenario) {
        return MAP.get(scenario);
    }

    public static List<Scenario> getValues(Type type) {
        return values.get(type);
    }

    private static List<Scenario> getAll() {
        return values.values().stream().flatMap(List::stream).toList();
    }

    public static void resetCache() {
        MAP.reset();
    }

    public static Map<String, List<Scenario>> getLazyMap() {
        return MAP.getCurrentMap();
    }

    public static List<Scenario> getRequiredScenarios(Scenario scenario) {
        return scenario.required()
                .stream()
                .map(ScenarioManager::find)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
