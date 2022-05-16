package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StaffManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger(StaffManager.class);
    private static final List<Staff> staff = new ArrayList<>();

    public static CompletableFuture<Void> load() {
        return TaskScheduler.runAsync(() -> {
            ResultSet resultSet = Stats.getPostgresHandler().getSet("SELECT * FROM public.staff");
            while(resultSet.next()) {
                String username = resultSet.getString("username");
                String displayName = resultSet.getString("displayName");
                staff.add(new Staff(username, displayName));
            }
            LOGGER.info("Loaded {} staff members.", staff.size());
        });

    }

    public static void addStaff(Staff staff) {
        StaffManager.staff.add(staff);
        Stats.getPostgresHandler().executeUpdate("INSERT INTO public.staff (username, displayName) VALUES (?, ?)", ps -> {
            ps.setString(1, staff.username());
            ps.setString(2, staff.displayName());
        });
    }

    public static Optional<Staff> findStaff(String name) {
        return staff.stream().filter(staff -> staff.username().equalsIgnoreCase(name) || staff.displayName().equalsIgnoreCase(name)).findFirst();
    }

    public static List<Staff> getStaff() {
        return staff;
    }
}
