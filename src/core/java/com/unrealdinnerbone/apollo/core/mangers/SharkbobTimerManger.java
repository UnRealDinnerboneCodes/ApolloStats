package com.unrealdinnerbone.apollo.core.mangers;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.IManger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SharkbobTimerManger implements IManger {

    private final Map<Long, SharkbobReset> sharkbobResets = new HashMap<>();

    @Override
    public void start() throws SQLException {
        ResultSet resultSet = Stats.INSTANCE.getPostgresHandler().getSet("SELECT * FROM public.sharkbob");
        while (resultSet.next()) {
            long time = resultSet.getLong("time");
            String message = resultSet.getString("message");
            String reseter = resultSet.getString("reseter");
            sharkbobResets.put(time, new SharkbobReset(time, message, reseter));
        }
    }

    public void addSharkbobReset(SharkbobReset sharkbobReset) {
        sharkbobResets.put(sharkbobReset.time, sharkbobReset);
        Stats.INSTANCE.getPostgresHandler().executeUpdate("INSERT INTO public.sharkbob (time, message, reseter) VALUES (?, ?, ?)", ps -> {
            ps.setLong(1, sharkbobReset.time);
            ps.setString(2, sharkbobReset.message);
            ps.setString(3, sharkbobReset.reseter);
        });
    }

    public Map<Long, SharkbobReset> getSharkbobResets() {
        return sharkbobResets;
    }

    public record SharkbobReset(long time, String message, String reseter) {

    }
}
