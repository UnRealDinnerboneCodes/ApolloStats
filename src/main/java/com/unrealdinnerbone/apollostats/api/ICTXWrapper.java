package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.Stats;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.http.HttpStatus;

public interface ICTXWrapper {
    String queryParam(String param);

    String pathParam(String param);

    void html(String html);

    void error(HttpCode status, String errorMessage);


    static ICTXWrapper of(Context handler) {
        return new ICTXWrapper() {
            @Override
            public String queryParam(String param) {
                return handler.queryParam(param);
            }

            @Override
            public String pathParam(String param) {
                return handler.pathParam(param);
            }

            @Override
            public void html(String html) {
                handler.html(html);
            }
            @Override
            public void error(HttpCode status, String errorMessage) {
                handler.status(status).html(Stats.getResourceAsString("error.html")
                        .replace("{Error_Code}", status.getStatus() + " (" + status.getMessage() + ")")
                        .replace("{Error_Message}", errorMessage));
            }
        };
    }
}
