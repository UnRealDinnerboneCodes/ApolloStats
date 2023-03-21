package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.web.ApolloRole;
import io.javalin.http.Context;

public interface IWebPage {
    void getPage(Context handler);
    String getPath();
    default ApolloRole getRole() {
        return ApolloRole.EVERYONE;
    }



}
