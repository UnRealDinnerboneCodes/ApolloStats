package com.unrealdinnerbone.apollo.web;

import com.unrealdinnerbone.apollo.core.ApolloEventManager;
import com.unrealdinnerbone.apollo.core.api.event.IEvent;
import com.unrealdinnerbone.apollo.core.api.module.IModule;
import com.unrealdinnerbone.apollo.core.api.module.Module;
import com.unrealdinnerbone.apollo.web.mangers.PageManger;

import java.util.concurrent.CompletableFuture;

@Module("web")
public class WebModule implements IModule {



    @Override
    public CompletableFuture<Void> start() {
        return new PageManger().start();
    }

    @Override
    public void registerEventHandlers(ApolloEventManager<IEvent> eventManager) {

    }
}
