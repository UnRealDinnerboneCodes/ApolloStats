package com.unrealdinnerbone.apollostats.lib;

import com.unrealdinnerbone.unreallib.MathHelper;
import com.unrealdinnerbone.unreallib.Triplet;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCServerPingResponse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util
{
    private static final String VAILD = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";

    public static final int MIN = 3;
    public static final int MAX = 9;

    public static String getMotdMessage(MCServerPingResponse result) {
        return PlainTextComponentSerializer.plainText().serialize(GsonComponentSerializer.gson().deserialize(result.description()));
    }
    public static String formalize(String s) {
        return s.toLowerCase().replace(" ", "").replace("+", "plus").replaceAll("\\p{Punct}+", "");
    }

    public static String createId(int listId, int min, int max) {
        String s = listId + "_" + createID();
        if(min != MIN || max != MAX) {
            s += "_" + min + "_" + max;
        }
        return s;
    }

    public static Triplet<Integer, Integer, Integer> decodeId(String id) {
        String[] split = id.split("_");
        int listId = Integer.parseInt(split[0]);
        if(split.length == 4) {
            return new Triplet<>(listId, Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        }else {
            return new Triplet<>(listId, MIN, MAX);
        }

    }

    public static String createID() {
        return IntStream.range(0, 5).mapToObj(i -> String.valueOf(VAILD.charAt(MathHelper.randomInt(0, VAILD.length())))).collect(Collectors.joining());
    }

    public static Instant utcNow() {
        return Instant.now().atZone(ZoneId.of("UTC")).toInstant();
    }
}
