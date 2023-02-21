package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.IManger;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.unreallib.list.LazyHashMap;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.list.Maps;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScenarioManager implements IManger
{
    private static final Logger LOGGER = LogHelper.getLogger();

    private final LevenshteinDistance LEVENSHTEIN_DISTANCE = LevenshteinDistance.getDefaultInstance();
    private final Map<Type, List<Scenario>> values = new HashMap<>();
    private final Map<String, List<Scenario>> remap = new HashMap<>();
    private final LazyHashMap<String, List<Scenario>> MAP = new LazyHashMap<>(cache -> {
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



    public boolean isSimilar(String name, String other) {
        return Util.formalize(name).equalsIgnoreCase(Util.formalize(other));
    }


    public void loadScenData() throws SQLException {
        values.clear();
        Arrays.stream(Type.values()).forEach(value -> values.put(value, new ArrayList<>()));
        ResultSet resultSet = Stats.INSTANCE.getPostgresHandler().getSet("SELECT * FROM public.scenario");
        while(resultSet.next()) {
            String name = resultSet.getString("name");
            Type type = Type.fromString(resultSet.getString("type"));
            int id = resultSet.getInt("id");
            boolean isActive = resultSet.getBoolean("image");
            boolean official = resultSet.getBoolean("official");
            Integer[] required = (Integer[]) resultSet.getArray("required").getArray();
            Integer[] disallowed = (Integer[]) resultSet.getArray("disallowed").getArray();
            boolean hostable = resultSet.getBoolean("hostable");
            boolean isMeta = resultSet.getBoolean("meta");
            values.get(type).add(new Scenario(name, id, isActive, official, type, Arrays.asList(required), Arrays.asList(disallowed), hostable, isMeta));
        }

    }

    public void loadRemapData() throws SQLException {
        remap.clear();
        ResultSet resultSet = Stats.INSTANCE.getPostgresHandler().getSet("SELECT * FROM public.scen_remap");
        while (resultSet.next()) {
            int scen = resultSet.getInt("scenId");
            String remap = resultSet.getString("name");
            find(scen).ifPresentOrElse(scenario -> {
                        Maps.putIfAbsent(this.remap, remap, new ArrayList<>());
                        if (!this.remap.get(remap).contains(scenario)) {
                            this.remap.get(remap).add(scenario);
                        }
                    },
                    () -> LOGGER.error("Failed to find scenario with id {} for remap {}", scen, remap));
        }
    }


    public List<Scenario> fix(Type type, List<String> fixed) {
        List<Scenario> cake = values.get(type)
                .stream()
                .map(Scenario::name)
                .map(this::remap)
                .flatMap(Collection::stream)
                .toList();
        return fixed.stream()
                .map(this::remap)
                .flatMap(Collection::stream)
                .filter(scenario -> cake.stream().anyMatch(scenario1 -> scenario1.name().equalsIgnoreCase(scenario.name())))
                .filter(scenario -> scenario.type() == type)
                .collect(Collectors.toList());

    }

    public Optional<Scenario> find(int id) {
        return values.values().stream().flatMap(Collection::stream).filter(scenario -> scenario.id() == id).findFirst();
    }

    private List<Scenario> remap(String scenario) {
        return MAP.get(scenario);
    }

    public List<Scenario> getValues(Type type) {
        return values.get(type);
    }

    private List<Scenario> getAll() {
        return values.values().stream().flatMap(List::stream).toList();
    }

    public void resetCache() {
        MAP.reset();
    }

    public Map<String, List<Scenario>> getLazyMap() {
        return MAP.getCurrentMap();
    }

    public List<Scenario> getRequiredScenarios(Scenario scenario) {
        return scenario.required()
                .stream()
                .map(this::find)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        TaskScheduler.scheduleRepeatingTaskExpectantly(1, TimeUnit.HOURS, task -> {
            loadScenData();
            loadRemapData();
            if(!future.isDone()) {
                future.complete(null);
            }
        }, e -> LOGGER.error("Failed to load scenarios", e));
        return future;
    }

    @Override
    public String getName() {
        return "Scenario Manager";
    }
}
