package com.unrealdinnerbone.apollo.discord;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.apollo.core.api.event.MatchEvents;
import com.unrealdinnerbone.apollo.core.api.event.UnknownScenarioEvent;
import com.unrealdinnerbone.unreallib.LogHelper;
import com.unrealdinnerbone.unreallib.SimpleColor;
import com.unrealdinnerbone.unreallib.TaskScheduler;
import com.unrealdinnerbone.unreallib.discord.DiscordWebhook;
import com.unrealdinnerbone.unreallib.discord.EmbedObject;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.awt.*;
import java.time.Instant;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AlertManager
{
    private static final Logger LOGGER = LogHelper.getLogger();
    private static final Queue<DiscordWebhook> WEBHOOKS = new LinkedList<>();


    public static TimerTask init() {
        return TaskScheduler.scheduleRepeatingTask(5, TimeUnit.SECONDS, task -> {
            DiscordWebhook discordWebhook = WEBHOOKS.poll();
            if(discordWebhook != null) {
                try {
                    discordWebhook.post(Stats.INSTANCE.getStatsConfig().getDiscordWebBotToken());
                } catch (IllegalStateException e) {
                    LOGGER.error("Failed to send webhook", e);
                } catch (WebResultException e) {
                    if(e.getCode() != 204) {
                        LOGGER.error("Failed to send webhook", e);
                    }
                }
            }
        });
    }
    public static void gameSaved(MatchEvents.GameSaved event) {
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(fromColor(Color.YELLOW))
                .author(event.match().findStaff().map(Staff::displayName).orElse("Unknown"), event.match().getUrl())
                .footer("Fill: " + event.game().fill())
                .description("Time: <t:{}:T>".replace("{}", String.valueOf(Instant.parse(event.match().opens()).toEpochMilli() / 1000)))
                .build()));
    }

    public static void unknownSceneFound(UnknownScenarioEvent event) {
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(fromColor(Color.BLUE))
                .description("Unknown Scenario Found: " + event.unknownScen() + " Guessed: " + Arrays.toString(event.guessed().toArray()))
                .build()));
    }

    public static void gameFound(MatchEvents.GameFound event) {
        Match match = event.match();
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(fromColor(Color.GREEN))
                .title("New Game: " + match.displayName() + " #" + match.count())
                .field("Meetup", String.valueOf(match.length()), true)
                .field("Nether", match.getNetherFormat(), true)
                .field("PvP", String.valueOf(match.pvpEnabledAt()), true)
                .field("Border", String.valueOf(match.mapSize()), true)
                .field("Team", match.getTeamFormat() + "(" + match.getTeamSize() + ")", true)
                .field("Opens", "<t:{}:T>".replace("{}", String.valueOf(Instant.parse(match.opens()).getEpochSecond())), true)
                .url(match.getUrl())
                .footer(String.join(", ", match.scenarios()), null)
                .build()));
    }


    public static void gameRemoved(MatchEvents.GameRemoved events) {
        Match match = events.match();
        addWebhook(hook -> hook.addEmbed(EmbedObject.builder()
                .color(fromColor(Color.RED))
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

    @NotNull
    static SimpleColor fromColor(java.awt.Color color) {
        return SimpleColor.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }

}
