package com.unrealdinnerbone.apollostats.mangers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.BingoValue;
import com.unrealdinnerbone.apollostats.api.IManger;
import com.unrealdinnerbone.unreallib.list.ArrayUtil;
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

public class BingoManger implements IManger {
    private static final Logger LOGGER = LoggerFactory.getLogger(BingoManger.class);
    private final Cache<String, String> bingoCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private final List<BingoValue> bingoValues = new ArrayList<>();

    public List<BingoValue> getBingoValues() {
        return bingoValues;
    }

    @Override
    public CompletableFuture<Void> start() {
        return TaskScheduler.runAsync(() -> {
            bingoValues.clear();
            ResultSet resultSet = Stats.INSTANCE.getPostgresHandler().getSet("SELECT * FROM public.bingo");
            while(resultSet.next()) {
                bingoValues.add(new BingoValue(resultSet.getString("bingo"), resultSet.getBoolean("auto_win"), resultSet.getBoolean("is_player")));
            }
        });
    }

    public String getBingoCard(String id, boolean players, String freeSpace) {
        try {
            return bingoCache.get(id, () -> findBingoData(id)
                    .or(() -> createCard(id, players, freeSpace))
                    .map(BingoData::toHtml)
                    .orElseThrow(() -> new RuntimeException("Could not create bingo card")));
        }catch(ExecutionException | RuntimeException e) {
            LOGGER.error("Error getting bingo card", e);
            return Stats.getResourceAsString("/error.html");
        }
    }

    public Optional<String> findCard(String id) {
        return Optional.ofNullable(bingoCache.getIfPresent(id)).or(() -> findBingoData(id).map(BingoData::toHtml));
    }


    private Optional<BingoData> createCard(String id, boolean includePlayers, String freeSpace) {
        List<BingoValue> values = new ArrayList<>(bingoValues.stream().filter(bingoValue -> includePlayers || bingoValue.isPlayer()).toList());
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
                    Stats.INSTANCE.getPostgresHandler().executeUpdate("INSERT INTO public.cards (id, values, freespace, bingos) VALUES (?, ?, ?, ?)", statement -> {
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
        ResultSet resultSet = Stats.INSTANCE.getPostgresHandler().getSet("SELECT * FROM public.cards where id = ?", statement -> statement.setString(1, id));
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

    @Override
    public String getName() {
        return "Bingo Manager";
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
