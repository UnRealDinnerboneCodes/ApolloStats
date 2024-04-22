package com.unrealdinnerbone.apollo.core.lib;

import com.unrealdinnerbone.unreallib.MathHelper;
import com.unrealdinnerbone.unreallib.minecraft.ping.MCServerPingResponse;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy MMM dd")
            .withLocale(Locale.UK)
            .withZone(ZoneId.of("UTC"));

    private static final String VAILD = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";

    public static String getMotdAsString(MCServerPingResponse result) {
        return PlainTextComponentSerializer.plainText().serialize(GsonComponentSerializer.gson().deserialize(result.description().json()));
    }

    public static String formalize(String s) {
        return s.toLowerCase().replace(" ", "").replace("+", "plus").replaceAll("\\p{Punct}+", "");
    }

    public static String createID() {
        return IntStream.range(0, 5).mapToObj(i -> String.valueOf(VAILD.charAt(MathHelper.randomInt(0, VAILD.length() - 1)))).collect(Collectors.joining());
    }

    public static String formatData(Instant instant) {
        return FORMATTER.format(instant);
    }

}
