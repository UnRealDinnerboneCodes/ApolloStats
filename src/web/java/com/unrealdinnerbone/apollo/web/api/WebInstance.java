package com.unrealdinnerbone.apollo.web.api;


import io.javalin.config.JavalinConfig;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class WebInstance<T extends IWebPage> {

    private final int port;
    private final Consumer<JavalinConfig> config;

    public WebInstance(int port, Consumer<JavalinConfig> config) {
        this.port = port;
        this.config = config;
    }


    public abstract Map<Type, List<T>> getPages();

    public int getPort() {
        return port;
    }

    public Consumer<JavalinConfig> getConfig() {
        return config;
    }

    public enum Type {
        POST,
        GET,
        ;
    }
}
