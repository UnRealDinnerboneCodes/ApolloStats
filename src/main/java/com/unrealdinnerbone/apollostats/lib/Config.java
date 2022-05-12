package com.unrealdinnerbone.apollostats.lib;

import com.unrealdinnerbone.config.IConfigCreator;
import com.unrealdinnerbone.config.config.StringConfig;

public class Config {

    private final StringConfig PUSH_API_KEY;
    private final StringConfig DISCORD_WEBBOT_TOKEN;

    public Config(IConfigCreator configCreator) {
        this.PUSH_API_KEY = configCreator.createString("PUSH_API_KEY", "");
        this.DISCORD_WEBBOT_TOKEN = configCreator.createString("DISCORD_TOKEN", "");
    }

    public String getPushApiKey() {
        return PUSH_API_KEY.getValue();
    }

    public String getDiscordWebBotToken() {
        return DISCORD_WEBBOT_TOKEN.getValue();
    }
}
