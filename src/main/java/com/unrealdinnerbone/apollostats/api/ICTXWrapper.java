package com.unrealdinnerbone.apollostats.api;

import io.javalin.http.Context;

public interface ICTXWrapper {
    String queryParam(String param);

    String pathParam(String param);


    public static ICTXWrapper of(Context handler) {
        return new ICTXWrapper() {
            @Override
            public String queryParam(String param) {
                return handler.queryParam(param);
            }

            @Override
            public String pathParam(String param) {
                return handler.pathParam(param);
            }
        };
    }
}
