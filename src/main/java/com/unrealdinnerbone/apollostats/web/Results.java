package com.unrealdinnerbone.apollostats.web;

import com.unrealdinnerbone.unreallib.json.JsonUtil;

public class Results
{
    public record Message(String message) {}


    public static String message(String message) {
        return JsonUtil.DEFAULT.toJson(new Message(message));
    }
}
