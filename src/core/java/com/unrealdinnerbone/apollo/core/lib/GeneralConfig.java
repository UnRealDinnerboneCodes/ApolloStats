package com.unrealdinnerbone.apollo.core.lib;


import com.unrealdinnerbone.config.api.ConfigCreator;
import com.unrealdinnerbone.config.config.ConfigValue;

public class GeneralConfig {

    private final ConfigValue<String> DISCORD_WEBBOT_TOKEN;
    private final ConfigValue<String> SERVER_IP;
    private final ConfigValue<Integer> SERVER_PORT;
    private final ConfigValue<String> MATCH_PAGE;
    private final ConfigValue<String> DEFAULT_FREE_SPACE;
    private final ConfigValue<Boolean> ENABLE_MATCH_WATCHING;

    public GeneralConfig(ConfigCreator configCreator) {
        this.DISCORD_WEBBOT_TOKEN = configCreator.createString("DISCORD_TOKEN", "");
        this.SERVER_IP = configCreator.createString("SERVER_IP", "apollouhc.com");
        this.SERVER_PORT = configCreator.createInteger("SERVER_PORT", 25565);
        this.MATCH_PAGE = configCreator.createString("MATCH_PAGE", "https://hosts.uhc.gg/m/");
        this.DEFAULT_FREE_SPACE = configCreator.createString("DEFAULT_FREE_SPACE", "Free Space!");
        this.ENABLE_MATCH_WATCHING = configCreator.createBoolean("ENABLE_MATCH_WATCHING", true);
    }


    public String getDiscordWebBotToken() {
        return DISCORD_WEBBOT_TOKEN.get();
    }

    public String getServerIp() {
        return SERVER_IP.get();
    }

    public int getServerPort() {
        return SERVER_PORT.get();
    }

    public String getMatchPage() {
        return MATCH_PAGE.get();
    }

    public String getDefaultFreeSpace() {
        return DEFAULT_FREE_SPACE.get();
    }

    public boolean watchMatches() {
        return ENABLE_MATCH_WATCHING.get();
    }
}
