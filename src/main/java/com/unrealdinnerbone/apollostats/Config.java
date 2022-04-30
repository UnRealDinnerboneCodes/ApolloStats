package com.unrealdinnerbone.apollostats;

import com.unrealdinnerbone.config.IConfigCreator;
import com.unrealdinnerbone.config.config.StringConfig;

public class Config {

    private final StringConfig PUSH_API_KEY;

    public Config(IConfigCreator configCreator) {
        this.PUSH_API_KEY = configCreator.createString("PUSH_API_KEY", "");
    }

    public String getPushApiKey() {
        return PUSH_API_KEY.getValue();
    }
}
