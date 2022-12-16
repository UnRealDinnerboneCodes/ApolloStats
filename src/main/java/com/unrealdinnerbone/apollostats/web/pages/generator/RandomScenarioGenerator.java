package com.unrealdinnerbone.apollostats.web.pages.generator;

import com.unrealdinnerbone.apollostats.api.ICTXWrapper;
import com.unrealdinnerbone.apollostats.api.IStatPage;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.RandomScenManger;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.MathHelper;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RandomScenarioGenerator implements IStatPage {

    private static final Logger LOGGER = LogHelper.getLogger();
    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        try {
            wrapper.html(RandomScenManger.getPage(Util.createID(), MathHelper.randomInt(3, 7)));
        }catch(SQLException e) {
            LOGGER.error("Database Error", e);
            wrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
        catch(Exception e) {
            LOGGER.error("Error while create random scens", e);
            wrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error while create random scens");
        }
    }

    @Override
    public String getPath() {
        return "random_game";
    }

    public static class IDPage implements IStatPage {

        @Override
        public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
            try {
                String id = wrapper.pathParam("id");
                wrapper.html(RandomScenManger.find(id).orElseThrow(() ->  new RuntimeException("Can't find page with id: " + id)));
            }catch(SQLException e) {
                wrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
            }
            catch(Exception e) {
                wrapper.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        @Override
        public String getPath() {
            return "random_game/{id}";
        }
    }



}
