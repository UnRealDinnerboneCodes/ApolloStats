package com.unrealdinnerbone.apollo.web.instacnes;

import com.unrealdinnerbone.apollo.web.api.IStatPage;
import com.unrealdinnerbone.apollo.web.api.WebInstance;
import io.javalin.http.staticfiles.Location;

import java.util.List;
import java.util.Map;

public class PublicInstance extends WebInstance<IStatPage> {

    private final List<IStatPage> statPages;

    public PublicInstance(List<IStatPage> statPages) {
        super(1000, javalinConfig -> javalinConfig.staticFiles.add("/web", Location.CLASSPATH));
        this.statPages = statPages;
    }

    @Override
    public Map<Type, List<IStatPage>> getPages() {
        return Map.of(Type.GET, statPages);
    }
}