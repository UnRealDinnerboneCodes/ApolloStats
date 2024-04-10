package com.unrealdinnerbone.apollo.web.api;

import io.javalin.http.Context;

public interface IWebPage {

    void getPage(Context handler);

    String getPath();

}
