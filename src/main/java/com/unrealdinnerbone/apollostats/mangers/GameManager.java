package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.postgresslib.PostgresConsumer;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GameManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GameManager.class);
    private static final List<Game> games = new ArrayList<>();

    public static CompletableFuture<Void> init() {
        return TaskScheduler.runAsync(() -> {
            ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.games");
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                int fill = resultSet.getInt("fill");
                games.add(new Game(id, fill));
            }
            LOGGER.info("Loaded {} games", games.size());
        });
    }

    public static void addGames(List<Game> games) {
        GameManager.games.addAll(games);
        Stats.getPostgresHandler().executeBatchUpdate("INSERT INTO public.games (id, fill) VALUES (?, ?)", games.stream().<PostgresConsumer>map(game -> ps -> {
            ps.setInt(1, game.id());
            ps.setInt(2, game.fill());
        }).toList());
    }

    public static Optional<Game> findGame(int id) {
        return games.stream().filter(game -> game.id() == id).findFirst();
    }

    public static List<Game> getGames() {
        return games;
    }
}
