package com.unrealdinnerbone.apollo.web;

import com.unrealdinnerbone.apollo.core.ApolloEventManager;
import com.unrealdinnerbone.apollo.core.api.event.IEvent;
import com.unrealdinnerbone.apollo.core.api.module.IModule;
import com.unrealdinnerbone.apollo.core.api.module.Module;
import com.unrealdinnerbone.apollo.web.mangers.PageManger;
import com.unrealdinnerbone.unreallib.LogHelper;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Module("web")
public class WebModule implements IModule {


    private static final Logger LOGGER = LogHelper.getLogger();


    @Override
    public void start() {
        LOGGER.info("Starting Web Module");
        new PageManger().start();
    }

    @Override
    public void registerEventHandlers(ApolloEventManager<IEvent> eventManager) {

    }
}
