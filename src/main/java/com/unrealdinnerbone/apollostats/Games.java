package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.exception.ExceptionConsumer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Games
{
    private static final List<Game> games = new ArrayList<>();

    public static void loadData(PostgressHandler postgressHandler) throws SQLException {
        games.clear();
        ResultSet resultSet = postgressHandler.getSet("SELECT * FROM public.games");
        while(resultSet.next()) {
            int id = resultSet.getInt("id");
            int fill = resultSet.getInt("fill");
            games.add(new Game(id, fill));
        }
    }

    public static void addGames(PostgressHandler handler, List<Game> games) {
        handler.executeBatchUpdate("INSERT INTO public.games (id, fill) VALUES (?, ?)", games.stream().<ExceptionConsumer<PreparedStatement, SQLException>>map(game -> ps -> {
            ps.setInt(1, game.id());
            ps.setInt(2, game.fill());
        }).toList());
    }

    public static List<Game> getGames() {
        return games;
    }
}
