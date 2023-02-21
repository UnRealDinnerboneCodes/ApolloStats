package com.unrealdinnerbone.apollostats.mangers;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.api.Game;
import com.unrealdinnerbone.apollostats.api.Match;
import com.unrealdinnerbone.apollostats.api.Scenario;
import com.unrealdinnerbone.apollostats.api.Staff;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.discord.DiscordWebhook;
import com.unrealdinnerbone.unreallib.discord.EmbedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AlertManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertManager.class);
    private static final Queue<DiscordWebhook> WEBHOOKS = new LinkedList<>();


    public static TimerTask init() {
        return TaskScheduler.scheduleRepeatingTask(5, TimeUnit.SECONDS, task -> {
            DiscordWebhook discordWebhook = WEBHOOKS.poll();
            if(discordWebhook != null) {
                try {
                    discordWebhook.post(Stats.INSTANCE.getStatsConfig().getDiscordWebBotToken());
                } catch (InterruptedException | IOException e) {
                    LOGGER.error("Failed to send webhook", e);
                }
            }
        });
    }
    public static void gameSaved(Match match, Game game) {
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(Color.YELLOW)
                .author(match.findStaff().map(Staff::displayName).orElse("Unknown"), match.getUrl())
                .footer("Fill: " + game.fill())
                .description("Time: <t:{}:T>".replace("{}", String.valueOf(Instant.parse(match.opens()).toEpochMilli() / 1000)))
                .build()));
    }

    public static void unknownSceneFound(String scen, List<Scenario> guessed) {
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(Color.BLUE)
                .description("Unknown Scenario Found: " + scen + " Guessed: " + Arrays.toString(guessed.toArray()))
                .build()));
    }

    public static void gameFound(Match match) {
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(Color.GREEN)
                .title("New Game: " + match.displayName() + " #" + match.count())
                .field("Meetup", String.valueOf(match.length()), true)
                .field("Nether", match.isNether(), true)
                .field("PvP", String.valueOf(match.pvpEnabledAt()), true)
                .field("Border", String.valueOf(match.mapSize()), true)
                .field("Team", String.valueOf(match.getTeamFormat()), true)
                .field("Opens", "<t:{}:T>".replace("{}", String.valueOf(Instant.parse(match.opens()).getEpochSecond())), true)
                .url(match.getUrl())
                .footer(String.join(", ", match.scenarios()), null)
                .build()));
    }


    public static void gameRemoved(Match match) {
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(Color.RED)
                .title("Game Removed: " + match.displayName() + " #" + match.count())
                .description("Reason: " + match.removedReason())
                .url(match.getUrl())
                .build()));
    }

    private static void addWebhook(Consumer<DiscordWebhook> consumer) {
        DiscordWebhook webhook = DiscordWebhook.builder();
        consumer.accept(webhook);
        WEBHOOKS.add(webhook);
    }

}
