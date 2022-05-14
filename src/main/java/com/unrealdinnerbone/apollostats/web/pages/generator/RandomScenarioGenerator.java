package com.unrealdinnerbone.apollostats.web.pages.generator;

import com.google.common.collect.Lists;
import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.BingoManger;
import com.unrealdinnerbone.apollostats.mangers.RandomScenManger;
import com.unrealdinnerbone.apollostats.mangers.ScenarioManager;
import com.unrealdinnerbone.unreallib.ArrayUtil;
import com.unrealdinnerbone.unreallib.MathHelper;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomScenarioGenerator implements IStatPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomScenarioGenerator.class);
    @Override
    public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
        try {
            wrapper.html(RandomScenManger.getPage(Util.createID(), MathHelper.randomInt(3, 7)));
        }catch(SQLException e) {
            LOGGER.error("Database Error", e);
            wrapper.error(HttpCode.INTERNAL_SERVER_ERROR, "Database error");
        }
        catch(Exception e) {
            LOGGER.error("Error while create random scens", e);
            wrapper.error(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage());
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
                wrapper.error(HttpCode.INTERNAL_SERVER_ERROR, "Database error");
            }
            catch(Exception e) {
                wrapper.error(HttpCode.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        @Override
        public String getPath() {
            return "random_game/{id}";
        }
    }



}
