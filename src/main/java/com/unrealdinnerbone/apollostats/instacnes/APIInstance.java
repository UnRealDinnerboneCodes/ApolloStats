package com.unrealdinnerbone.apollostats.instacnes;

import com.unrealdinnerbone.apollostats.api.IWebPage;
import com.unrealdinnerbone.apollostats.api.WebInstance;

import java.util.List;
import java.util.Map;

public class APIInstance extends WebInstance<IWebPage> {

    private final List<IWebPage> webPages;

    public APIInstance(List<IWebPage> webPages) {
        super(1001, javalinConfig -> {});
        this.webPages = webPages;
    }

    @Override
    public Map<Type, List<IWebPage>> getPages() {
        return Map.of(Type.POST, webPages);
    }
}
