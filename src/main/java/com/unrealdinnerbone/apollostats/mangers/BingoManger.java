package com.unrealdinnerbone.apollostats.mangers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.BingoValue;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BingoManger {

    private static final Cache<String, String> BINGO_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private static final Logger LOGGER = LoggerFactory.getLogger(BingoManger.class);
    private static final List<BingoValue> BINGO_VALUES = new ArrayList<>();


    public static CompletableFuture<Void> init() {
        return TaskScheduler.runAsync(() -> {
            BINGO_VALUES.clear();
            ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.bingo");
            while(resultSet.next()) {
                BINGO_VALUES.add(new BingoValue(resultSet.getString("bingo"), resultSet.getBoolean("auto_win"), resultSet.getBoolean("is_player")));
            }
        });
    }

    public static List<BingoValue> getBingoValues() {
        return BINGO_VALUES;
    }

    public static String getBingoCard(String id, PostgressHandler postgressHandler, boolean players, String freeSpace) {
        Random random = new Random(id.hashCode());
        try {
            return BINGO_CACHE.get(id, () -> {
                ResultSet resultSet = postgressHandler.getSet("SELECT * FROM public.cards where id = ?", statement -> statement.setString(1, id));
                if(resultSet.next()) {
                    String values = resultSet.getString("values");
                    String theFreespace = resultSet.getString("freespace");
                    String[] split = values.split(";");
                    String[] frocedBingos = resultSet.getString("bingos").split(";");
                    return getCard(List.of(split), List.of(frocedBingos), theFreespace, "");
                }else {
                    List<BingoValue> values = new ArrayList<>(BINGO_VALUES.stream().filter(bingoValue -> players || bingoValue.isPlayer()).toList());
                    if(values.size() >= 25) {
                        List<String> list = new ArrayList<>();
                        List<String> forced = new ArrayList<>();
                        for(int i = 0; i < 25; i++) {
                            BingoValue bingoValue = ArrayUtil.getRandomValueAndRemove(random, values);
                            list.add(bingoValue.bingo());
                            if(bingoValue.isBingo()) {
                                forced.add(bingoValue.bingo());
                            }
                        }
                        TaskScheduler.handleTaskOnThread(() -> {
                            try {
                                postgressHandler.executeUpdate("INSERT INTO public.cards (id, values, freespace, bingos) VALUES (?, ?, ?, ?)", statement -> {
                                    statement.setString(1, id);
                                    statement.setString(2, String.join(";", list));
                                    statement.setString(3, freeSpace);
                                    statement.setString(4, String.join(";", forced));
                                });
                            }catch(Exception e) {
                                LOGGER.error("Error while inserting bingo card", e);
                            }
                        });
                        return getCard(list, forced, freeSpace, id);

                    }else {
                        throw new RuntimeException("Not enough bingo values");
                    }
                }

            });
        }catch(ExecutionException | UncheckedExecutionException e) {
            LOGGER.error("Error getting bingo card", e);
            return
                    "";
//            return getResourceAsString("/500.html");
        }
    }

    public static String getCard(List<String> values, List<String> forced, String freeSpace, String id) {
        return "<!DOCTYPE html>\n";
//        return getResourceAsString("bingo.html")
//                .replace("{\"BINGO_VALUES\"}", format(values))
//                .replace("{\"BINGO_WORDS\"}", format(forced))
//                .replace("{\"FREE_SPACE\"}", freeSpace)
//                .replace("{\"URL\"}", id);
    }

    public static String format(List<String> values) {
        return values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
    }
}
