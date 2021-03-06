package com.unrealdinnerbone.apollostats.lib;

import com.unrealdinnerbone.unreallib.MathHelper;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCServerPingResponse;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util
{
    private static final String VAILD = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";

    public static String getMotdMessage(MCServerPingResponse result) {
        return PlainTextComponentSerializer.plainText().serialize(GsonComponentSerializer.gson().deserialize(result.description()));
    }
    public static String formalize(String s) {
        return s.toLowerCase().replace(" ", "").replace("+", "plus").replaceAll("\\p{Punct}+", "");
    }

    public static String createID() {
        return IntStream.range(0, 5).mapToObj(i -> String.valueOf(VAILD.charAt(MathHelper.randomInt(0, VAILD.length())))).collect(Collectors.joining());
    }

    public static Instant utcNow() {
        return Instant.now().atZone(ZoneId.of("UTC")).toInstant();
    }
}
