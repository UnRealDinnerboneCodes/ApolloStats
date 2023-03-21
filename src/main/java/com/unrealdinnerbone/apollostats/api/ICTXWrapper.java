package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.Stats;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICTXWrapper extends ICTXGetter {
    void html(String html);

    void error(HttpStatus status, String errorMessage);



    static ICTXWrapper of(Context handler) {
        return new ICTXWrapper() {
            @Override
            public Optional<String> queryParam(String param) {
                return Optional.ofNullable(handler.queryParam(param));
            }

            @Override
            public String pathParam(String param) {
                return handler.pathParam(param);
            }

            @Override
            public String getRequestID() {
                return handler.path() + handler.queryString();
            }

            @Override
            public void html(String html) {
                handler.html(html);
            }
            @Override
            public void error(HttpStatus status, String errorMessage) {
                handler.status(status).html(Stats.getResourceAsString("/error.html")
                        .replace("{Error_Code}", status.getMessage() + " (" + status.getMessage() + ")")
                        .replace("{Error_Message}", errorMessage));
            }

            @Override
            public Map<String, List<String>> getQueryPerms() {
                return new HashMap<>(handler.queryParamMap());
            }
        };
    }
}
