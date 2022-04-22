package com.unrealdinnerbone.apollostats;

import com.squareup.moshi.Moshi;
import com.unrealdinnerbone.apollostats.temp.temp.RecordsJsonAdapterFactory;
import com.unrealdinnerbone.unreallib.MathHelper;
import com.unrealdinnerbone.unreallib.Triplet;
import com.unrealdinnerbone.unreallib.json.IJsonParser;
import com.unrealdinnerbone.unreallib.json.MoshiParser;
import org.checkerframework.checker.units.qual.min;
import org.eclipse.jetty.util.IO;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util
{
    private static final String VAILD = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";

    public static final int MIN = 3;
    public static final int MAX = 9;

    public final static List<String> STAFF = Arrays.asList(
            "C_moneySmith",
            "adorablur",
            "AlgoHost",
            "AtomicCrossbow",
            "CarbonateCO3",
            "CxlibriPlays",
            "DaniDeVit0",
            "Dashetoboba",
            "ElTioDodo",
            "Gronyak124",
            "JJQ4",
            "DaDoshua",
            "zombi3s_",
            "MSIPig",
            "NeededCheese",
            "rachammc",
            "Sicced",
            "TheMainMiek",
            "ImHab",
            "CheetaaahReddit",
            "AyeeSammy14",
            "_UglySheep_",
            "WackMaDino",
            "p1an_",
            "sam03062",
            "Slushybunion",
            "TinyxNinja",
            "Mihkeeee",
            "Andronifyy",
            "LordJxck",
            "PingasPootis",
            "DIISU",
            "Gejin",
            "uh_joe",
            "Chugabunga",
            "Sharkbob94349",
            "MrDennis25",
            "rachammc");


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

    private static String createID() {
        return IntStream.range(0, 5).mapToObj(i -> String.valueOf(VAILD.charAt(MathHelper.randomInt(0, VAILD.length())))).collect(Collectors.joining());
    }
    public static boolean isNewLine(String s) {
        return s.equals("\n");
    }

    private static final IJsonParser<IOException> JSON_PARSER = new MoshiParser(new Moshi.Builder().add(new RecordsJsonAdapterFactory()).build());


    public static IJsonParser<IOException> parser() {
        return JSON_PARSER;
    }
}
