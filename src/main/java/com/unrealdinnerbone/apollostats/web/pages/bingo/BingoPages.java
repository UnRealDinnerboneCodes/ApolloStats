package com.unrealdinnerbone.apollostats.web.pages.bingo;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.BingoManger;
import com.unrealdinnerbone.apollostats.web.ApolloRole;
import com.unrealdinnerbone.apollostats.web.Results;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class BingoPages
{
    private static final Logger LOGGER = LogHelper.getLogger();
    public static class NewCard implements IStatPage {

        @Override
        public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) {
            boolean players = true;
            String defaultFreeSpace = Stats.CONFIG.getDefaultFreeSpace();
            String perm = wrapper.queryParam("players");
            if(perm != null && perm.equalsIgnoreCase("false")) {
                players = false;
            }
            String freespace = wrapper.queryParam("freespace");
            if(freespace != null) {
                defaultFreeSpace = freespace;
            }
            wrapper.html(BingoManger.getBingoCard(Util.createID(), players, defaultFreeSpace));
        }

        @Override
        public String getPath() {
            return "bingo";
        }
    }

    public static class IDCard implements IStatPage {

        @Override
        public void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper ctx) {
            String id = ctx.pathParam("id");
            ctx.html(BingoManger.findCard(id).orElse(Stats.getResourceAsString("/error.html")));
        }

        @Override
        public String getPath() {
            return "bingo/{id}";
        }
    }

    public static class Add implements IWebPage {


        @Override
        public ApolloRole getRole() {
            return ApolloRole.POST_API;
        }

        @Override
        public void getPage(Context handler) {
            try {
                BingoValue value = JsonUtil.DEFAULT.parse(BingoValue.class, handler.body());
                if(value == null) {
                    handler.status(HttpStatus.BAD_REQUEST).result(Results.message("Invalid JSON (How did you get here?)"));
                }else {
                    if(value.bingo() == null) {
                        handler.status(HttpStatus.BAD_REQUEST).result(Results.message("Bingo value is null"));
                    }else {
                        if(BingoManger.getBingoValues().contains(value)) {
                            handler.status(HttpStatus.BAD_REQUEST).result(Results.message("Bingo value already exists"));
                        }else {
                            BingoManger.getBingoValues().add(value);
                            LOGGER.info("Added {}", value);
                            Stats.getPostgresHandler().executeUpdate("INSERT INTO public.bingo (bingo, auto_win, is_player) VALUES (?, ?, ?)", statement -> {
                                statement.setString(1, value.bingo());
                                statement.setBoolean(2, value.isBingo());
                                statement.setBoolean(3, value.isPlayer());
                            });
                        }
                    }
                }
            }catch(Exception e) {
                handler.status(HttpStatus.BAD_REQUEST).result(Results.message(e.getMessage()));
            }
        }

        @Override
        public String getPath() {
            return "/v1/bingo/add";
        }
    }

}
