package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.LazyHashMap;
import com.unrealdinnerbone.unreallib.Pair;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScenarioManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioManager.class);
    private static final Map<Type, List<Scenario>> values = new HashMap<>();
    private static final LazyHashMap<String, Optional<Scenario>> MAP = new LazyHashMap<>(cache -> {
        Map<Integer, List<Scenario>> values = new HashMap<>();
        for(Scenario scenario : getAll()) {
            if(isSimilar(scenario.name(), cache)) {
                return Optional.of(scenario);
            }else {
                int id = LevenshteinDistance.getDefaultInstance().apply(scenario.name(), cache);
                if(!values.containsKey(id)) {
                    values.put(id, new ArrayList<>());
                }
                values.get(id).add(scenario);
            }
        }
        Optional<Pair<Integer, Optional<Scenario>>> lowestId = values.keySet()
                .stream()
                .min(Integer::compare)
                .filter(id -> id <= 9)
                .map(id -> Pair.of(id,Optional.of(values.get(id).get(0))));
        Pair<Integer, Optional<Scenario>> returnValue = lowestId.orElse(Pair.of(-1,Optional.empty()));
        LOGGER.info("{} -> {} ({})", cache, returnValue.value().map(Scenario::name).orElse("None"), returnValue.key());
        return returnValue.value();
    });

    private static boolean isSimilar(String name, String other) {
        return Util.formalize(name).equalsIgnoreCase(Util.formalize(other));
    }

    public static CompletableFuture<Void> init() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        TaskScheduler.scheduleRepeatingTaskExpectantly(1, TimeUnit.HOURS, task -> {
            ScenarioManager.loadData();
            if(!future.isDone()) {
                future.complete(null);
            }
        }, e -> LOGGER.error("Failed to load scenarios", e));
        return future;
    }

    public static void loadData() throws SQLException {
        values.clear();
        Arrays.stream(Type.values()).forEach(value -> values.put(value, new ArrayList<>()));
        ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.scenario");
        while(resultSet.next()) {
            String name = resultSet.getString("name");
            String type = resultSet.getString("type");
            int id = resultSet.getInt("id");
            boolean isActive = resultSet.getBoolean("image");
            boolean official = resultSet.getBoolean("official");
            Type type1 = Type.fromString(type);
            values.get(type1).add(new Scenario(name, id, isActive, official, type1));
        }

    }


    public static List<Scenario> fix(Type type, List<String> fixed) {
        List<Scenario> cake = values.get(type)
                .stream()
                .map(Scenario::name)
                .map(ScenarioManager::remap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return fixed.stream()
                .map(ScenarioManager::remap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(scenario -> cake.stream().anyMatch(scenario1 -> scenario1.name().equalsIgnoreCase(scenario.name())))
//                .filter(cake::contains)
                .filter(scenario -> scenario.type() == type)
                .collect(Collectors.toList());

    }

    public static Optional<Scenario> find(int id) {
        return values.values().stream().flatMap(Collection::stream).filter(scenario -> scenario.id() == id).findFirst();
    }

    private static Optional<Scenario> remap(String scenario) {
        return MAP.get(scenario.toLowerCase(Locale.ROOT));
    }

    public static List<Scenario> getValues(Type type) {
        return values.get(type);
    }

    private static List<Scenario> getAll() {
        return values.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
