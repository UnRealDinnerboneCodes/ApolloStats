package com.unrealdinnerbone.apollo.core.mangers;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Game;
import com.unrealdinnerbone.apollo.core.api.IManger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameManager implements IManger
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GameManager.class);

    private final List<Game> games = new ArrayList<>();

    public void addGame(Game games) {
        this.games.add(games);
        Stats.INSTANCE.getPostgresHandler().executeUpdate("INSERT INTO public.games (id, fill) VALUES (?, ?)", preparedStatement -> {
            preparedStatement.setInt(1, games.id());
            preparedStatement.setInt(2, games.fill());
        });
    }

    public Optional<Game> findGame(int id) {
        return games.stream().filter(game -> game.id() == id).findFirst();
    }

    public List<Game> getGames() {
        return games;
    }

    @Override
    public void start() throws SQLException {
        ResultSet resultSet = Stats.INSTANCE.getPostgresHandler().getSet("SELECT * FROM public.games");
        while(resultSet.next()) {
            int id = resultSet.getInt("id");
            int fill = resultSet.getInt("fill");
            games.add(new Game(id, fill));
        }
    }

}
