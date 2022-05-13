package com.unrealdinnerbone.apollostats.lib;

import com.unrealdinnerbone.config.IConfigCreator;
import com.unrealdinnerbone.config.config.IntegerConfig;
import com.unrealdinnerbone.config.config.StringConfig;

public class Config {

    private final StringConfig PUSH_API_KEY;
    private final StringConfig DISCORD_WEBBOT_TOKEN;
    private final StringConfig SERVER_IP;
    private final IntegerConfig SERVER_PORT;

    private final StringConfig MATCH_PAGE;

    public Config(IConfigCreator configCreator) {
        this.PUSH_API_KEY = configCreator.createString("PUSH_API_KEY", "");
        this.DISCORD_WEBBOT_TOKEN = configCreator.createString("DISCORD_TOKEN", "");
        this.SERVER_IP = configCreator.createString("SERVER_IP", "apollouhc.com");
        this.SERVER_PORT = configCreator.createInteger("SERVER_PORT", 25565);
        this.MATCH_PAGE = configCreator.createString("MATCH_PAGE", "https://hosts.uhc.gg/m/");
    }

    public String getPushApiKey() {
        return PUSH_API_KEY.getValue();
    }

    public String getDiscordWebBotToken() {
        return DISCORD_WEBBOT_TOKEN.getValue();
    }

    public String getServerIp() {
        return SERVER_IP.getValue();
    }

    public int getServerPort() {
        return SERVER_PORT.getValue();
    }

    public String getMatchPage() {
        return MATCH_PAGE.getValue();
    }
}