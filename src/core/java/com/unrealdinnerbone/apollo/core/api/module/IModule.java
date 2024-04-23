package com.unrealdinnerbone.apollo.core.api.module;

import com.unrealdinnerbone.apollo.core.ApolloEventManager;
import com.unrealdinnerbone.apollo.core.api.event.IEvent;

public interface IModule {
    void start();

    void registerEventHandlers(ApolloEventManager<IEvent> eventManager);
}
