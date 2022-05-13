package com.unrealdinnerbone.apollostats.mangers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.BingoValue;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BingoManger {
    private static final Logger LOGGER = LoggerFactory.getLogger(BingoManger.class);
    private static final Cache<String, String> BINGO_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

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

    public static String getBingoCard(String id, boolean players, String freeSpace) {
        try {
            return BINGO_CACHE.get(id, () -> findBingoData(id)
                    .or(() -> createCard(id, players, freeSpace))
                    .map(BingoData::toHtml)
                    .orElseThrow(() -> new RuntimeException("Could not create bingo card")));
        }catch(ExecutionException | RuntimeException e) {
            LOGGER.error("Error getting bingo card", e);
            return Stats.getResourceAsString("/error.html");
        }
    }

    public static Optional<String> findCard(String id) {
        return Optional.ofNullable(BINGO_CACHE.getIfPresent(id)).or(() -> findBingoData(id).map(BingoData::toHtml));
    }


    private static Optional<BingoData> createCard(String id, boolean includePlayers, String freeSpace) {
        List<BingoValue> values = new ArrayList<>(BINGO_VALUES.stream().filter(bingoValue -> includePlayers || bingoValue.isPlayer()).toList());
        if(values.size() >= 25) {
            Random random = new Random(id.hashCode());
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
                    Stats.getPostgresHandler().executeUpdate("INSERT INTO public.cards (id, values, freespace, bingos) VALUES (?, ?, ?, ?)", statement -> {
                        statement.setString(1, id);
                        statement.setString(2, String.join(";", list));
                        statement.setString(3, freeSpace);
                        statement.setString(4, String.join(";", forced));
                    });
                }catch(Exception e) {
                    LOGGER.error("Error while inserting bingo card", e);
                }
            });
            return Optional.of(new BingoData(list, forced, freeSpace, id));
        }else {
            return Optional.empty();
        }
}


    private static Optional<BingoData> findBingoData(String id) {
        ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.cards where id = ?", statement -> statement.setString(1, id));
        try {
            if(resultSet.next()) {
                String values = resultSet.getString("values");
                String theFreespace = resultSet.getString("freespace");
                String[] split = values.split(";");
                String[] frocedBingos = resultSet.getString("bingos").split(";");
                return Optional.of(new BingoData(List.of(split), List.of(frocedBingos), theFreespace, id));
            }else {
                return Optional.empty();
            }
        }catch(SQLException e) {
            LOGGER.error("Error finding bingo card", e);
            return Optional.empty();
        }
    }

    public static String format(List<String> values) {
        return values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
    }

    public record BingoData(List<String> values, List<String> forced, String freeSpace, String id) {
        public String toHtml() {
            return Stats.getResourceAsString("bingo.html")
                    .replace("{\"BINGO_VALUES\"}", format(values()))
                    .replace("{\"BINGO_WORDS\"}", format(forced()))
                    .replace("{\"FREE_SPACE\"}", freeSpace())
                    .replace("{\"URL\"}", id());
        }
    }

}
