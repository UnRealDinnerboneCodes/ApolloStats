package com.unrealdinnerbone.apollo.core.api.module;

import com.unrealdinnerbone.apollo.core.ApolloEventManager;
import com.unrealdinnerbone.apollo.core.api.event.IEvent;

import java.util.concurrent.CompletableFuture;

public interface IModule {
    CompletableFuture<Void> start();

    void registerEventHandlers(ApolloEventManager<IEvent> eventManager);
}
