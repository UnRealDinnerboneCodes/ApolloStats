package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.discord.DiscordWebhook;
import com.unrealdinnerbone.unreallib.discord.EmbedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;

public class AlertManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertManager.class);
    public static void gameSaved(Match match, Game game) {
        try {
            DiscordWebhook.of(Stats.CONFIG.getDiscordWebBotToken())
                    .addEmbed(EmbedObject.builder()
                            .color(Color.GREEN)
                            .author(new EmbedObject.Author(match.findStaff().map(Staff::displayName).orElse("Unknown"), match.getUrl(), null))
                            .footer(EmbedObject.Footer.of("Fill: " + game.fill(), null))
                            .description("Time: <t:{}:T>".replace("{}", String.valueOf(Instant.parse(match.opens()).toEpochMilli() / 1000)))
                            .build())
                    .execute();

        }catch(IOException | InterruptedException e) {
            LOGGER.error("Error sending discord message", e);
        }
    }

    public static void gameFound(Match match) {
        try {
            DiscordWebhook.of(Stats.CONFIG.getDiscordWebBotToken())
                    .addEmbed(EmbedObject.builder()
                            .color(Color.GREEN)
                            .author(new EmbedObject.Author(match.findStaff().map(Staff::displayName).orElse("Unknown"), match.getUrl(), null))
                            .footer(EmbedObject.Footer.of("New Game: " + match.displayName(), null))
                            .description("Time: <t:{}:T>".replace("{}", String.valueOf(Instant.parse(match.opens()).toEpochMilli()/ 1000)))
                            .build())
                    .execute();

        }catch(IOException | InterruptedException e) {
            LOGGER.error("Error sending discord message", e);
        }
    }

}
