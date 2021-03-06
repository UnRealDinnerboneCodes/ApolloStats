package com.unrealdinnerbone.apollostats.web;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.unreallib.json.JsonUtil;

public class Results
{
    public record Message(String message) {}


    public static String message(String message) {
        return JsonUtil.DEFAULT.toJson(Message.class, new Message(message));
    }
}
