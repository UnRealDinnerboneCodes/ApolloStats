package com.unrealdinnerbone.apollostats.web.pages.bingo;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.*;
import com.unrealdinnerbone.apollostats.lib.Util;
import com.unrealdinnerbone.apollostats.mangers.BingoManger;
import com.unrealdinnerbone.apollostats.web.ApolloRole;
import com.unrealdinnerbone.apollostats.web.Results;
import com.unrealdinnerbone.postgresslib.PostgressHandler;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.discord.DiscordWebhook;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BingoPages
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BingoPages.class);
    public static class NewCard implements IStatPage {

        @Override
        public String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper ctx) {
            String perm = ctx.queryParam("player");
            boolean isPlayer = perm == null || Boolean.parseBoolean(perm);
            String freeSpace = ctx.queryParam("freeSpace");
            return BingoManger.getBingoCard(Util.createID(), Stats.getPostgresHandler(), isPlayer, freeSpace == null ? "Cooldude Racism Mute": freeSpace);
        }

        @Override
        public String getPath() {
            return "bingo";
        }
    }

    public static class IDCard implements IStatPage {

        @Override
        public String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper ctx) {
            String id = ctx.pathParam("id");
            String perm = ctx.queryParam("player");
            boolean isPlayer = perm == null || Boolean.parseBoolean(perm);
            String freeSpace = ctx.queryParam("freeSpace");
            return BingoManger.getBingoCard(id, Stats.getPostgresHandler(), isPlayer, freeSpace == null ? "Cooldude Racism Mute": freeSpace);
        }

        @Override
        public String getPath() {
            return "bingo";
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
                    handler.status(HttpCode.BAD_REQUEST).result(Results.message("Invalid JSON (How did you get here?)"));
                }else {
                    if(value.bingo() == null) {
                        handler.status(HttpCode.BAD_REQUEST).result(Results.message("Bingo value is null"));
                    }else {
                        if(BingoManger.getBingoValues().contains(value)) {
                            handler.status(HttpCode.BAD_REQUEST).result(Results.message("Bingo value already exists"));
                        }else {
                            BingoManger.getBingoValues().add(value);
                            LOGGER.info("Added {}", value);
                            Stats.getPostgresHandler().executeUpdate("INSERT INTO public.bingo (bingo, auto_win, is_player) VALUES (?, ?, ?)", statement -> {
                                statement.setString(1, value.bingo());
                                statement.setBoolean(2, value.isBingo());
                                statement.setBoolean(3, value.isPlayer());
                            });
//                            TaskScheduler.handleTaskOnThread(() -> {
//                                try {
//                                    DiscordWebhook.of(CONFIG.getDiscordWebBotToken()).setContent("Added new bingo value: " + value.bingo()).execute();
//                                }catch(IOException | InterruptedException e) {
//                                    LOGGER.error("Error while sending message to discord", e);
//                                }
//                            });
                        }
                    }
                }
            }catch(Exception e) {
                handler.status(HttpCode.BAD_REQUEST).result(Results.message(e.getMessage()));
            }
        }

        @Override
        public String getPath() {
            return "/v1/bingo/add";
        }
    }

}
