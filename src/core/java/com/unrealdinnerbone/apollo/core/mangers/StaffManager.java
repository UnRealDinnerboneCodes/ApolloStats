package com.unrealdinnerbone.apollo.core.mangers;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.IManger;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.unreallib.LogHelper;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class StaffManager implements IManger
{
    private static final Logger LOGGER = LogHelper.getLogger();

    private final List<Staff> staff = new ArrayList<>();

    @Override
    public void start() throws SQLException {
        ResultSet resultSet = Stats.INSTANCE.getPostgresHandler().getSet("SELECT * FROM public.staff");
        while (resultSet.next()) {
            String username = resultSet.getString("username");
            String displayName = resultSet.getString("displayName");
            Date date = resultSet.getDate("start");
            String foundType = resultSet.getString("type");
            Staff.Type type = Staff.Type.fromString(foundType).orElseThrow(() -> new IllegalStateException("Unknown Staff Type " + foundType));
            staff.add(new Staff(username, date.toInstant(), displayName, type));
        }
        LOGGER.info("Loaded {} staff members.", staff.size());
    }


    public void addStaff(Staff staff) {
        this.staff.add(staff);
        Stats.INSTANCE.getPostgresHandler().executeUpdate("INSERT INTO public.staff (username, displayName) VALUES (?, ?)", ps -> {
            ps.setString(1, staff.username());
            ps.setString(2, staff.displayName());
        });
    }

    public Optional<Staff> findStaff(String name) {
        return staff.stream().filter(staff -> staff.username().equalsIgnoreCase(name) || staff.displayName().equalsIgnoreCase(name)).findFirst();
    }

    public List<Staff> getStaff() {
        return staff;
    }
}
