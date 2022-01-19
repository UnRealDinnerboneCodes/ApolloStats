package com.unrealdinnerbone.apollostats;

import java.util.Arrays;
import java.util.List;

public class Util
{
    public static List<String> STAFF = Arrays.asList(
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
            "DIISU","Gejin",
            "uh_joe",
            "Chugabunga",
            "Sharkbob94349",
            "MrDennis25",
            "rachammc");


    public static String formalize(String s) {
        return s.toLowerCase().replace(" ", "").replaceAll("\\p{Punct}+", "");
    }


    public static boolean isNewLine(String s) {
        return s.equals("\n");
    }
}
