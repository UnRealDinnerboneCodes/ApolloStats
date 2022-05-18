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
import java.util.stream.Collectors;

public class AlertManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertManager.class);
    public static void gameSaved(Match match, Game game) {
        try {
            DiscordWebhook.of(Stats.CONFIG.getDiscordWebBotToken())
                    .addEmbed(EmbedObject.builder()
                            .color(Color.YELLOW)
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
                            .title("New Game: " + match.displayName() + " #" + match.id())
                            .field(EmbedObject.Field.of("Meetup", String.valueOf(match.length()), true))
                            .field(EmbedObject.Field.of("Nether", String.valueOf(match.isNether()), true))
                            .field(EmbedObject.Field.of("PvP", String.valueOf(match.pvpEnabledAt()), true))
                            .field(EmbedObject.Field.of("Border", String.valueOf(match.size()), true))
                            .url(match.getUrl())
                            .footer(EmbedObject.Footer.of(String.join(", ", match.scenarios()), null))
                            .description("Opens: <t:{}:T>".replace("{}", String.valueOf(Instant.parse(match.opens()).toEpochMilli() / 1000)))
                            .build())
                    .execute();

        }catch(IOException | InterruptedException e) {
            LOGGER.error("Error sending discord message", e);
        }
    }


    public static void gameRemoved(Match match) {
        try {
            DiscordWebhook.of(Stats.CONFIG.getDiscordWebBotToken())
                    .addEmbed(EmbedObject.builder()
                            .color(Color.RED)
                            .title("Game Removed: " + match.displayName() + " #" + match.id())
                            .description("Reason: " + match.removedReason())
                            .url(match.getUrl())
                            .build())
                    .execute();

        }catch(IOException | InterruptedException e) {
            LOGGER.error("Error sending discord message", e);
        }
    }

}
