package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Type;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.LazyHashMap;
import com.unrealdinnerbone.unreallib.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Scenarios
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Scenarios.class);
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

    public static void loadData(PostgressHandler postgressHandler) throws SQLException {
        values.clear();
        Arrays.stream(Type.values()).forEach(value -> values.put(value, new ArrayList<>()));
        ResultSet resultSet = postgressHandler.getSet("SELECT * FROM public.scenario");
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
                .map(Scenarios::remap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return fixed.stream()
                .map(Scenarios::remap)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(cake::contains)
                .filter(scenario -> scenario.type() == type)
                .collect(Collectors.toList());

    }

    private static Optional<Scenario> remap(String scenario) {
        return MAP.get(scenario);
    }

    public static List<Scenario> getValues(Type type) {
        return values.get(type);
    }

    private static List<Scenario> getAll() {
        return values.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
