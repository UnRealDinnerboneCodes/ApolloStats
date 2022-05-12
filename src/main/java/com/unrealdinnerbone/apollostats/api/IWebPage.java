package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.web.ApolloRole;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IWebPage {
    void getPage(Context handler);

    String getPath();


    default ApolloRole getRole() {
        return ApolloRole.EVERYONE;
    }



}
