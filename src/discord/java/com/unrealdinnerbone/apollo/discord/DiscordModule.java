package com.unrealdinnerbone.apollo.discord;

import com.unrealdinnerbone.apollo.core.ApolloEventManager;
import com.unrealdinnerbone.apollo.core.api.event.IEvent;
import com.unrealdinnerbone.apollo.core.api.event.MatchEvents;
import com.unrealdinnerbone.apollo.core.api.event.UnknownScenarioEvent;
import com.unrealdinnerbone.apollo.core.api.module.IModule;
import com.unrealdinnerbone.apollo.core.api.module.Module;

import java.util.concurrent.CompletableFuture;

@Module("discord")
public class DiscordModule implements IModule {

    @Override
    public CompletableFuture<Void> start() {
        AlertManager.init();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void registerEventHandlers(ApolloEventManager<IEvent> eventManager) {
        eventManager.registerHandler(MatchEvents.GameSaved.class, AlertManager::gameSaved);
        eventManager.registerHandler(MatchEvents.GameFound.class, AlertManager::gameFound);
        eventManager.registerHandler(MatchEvents.GameRemoved.class, AlertManager::gameRemoved);
        eventManager.registerHandler(UnknownScenarioEvent.class, AlertManager::unknownSceneFound);
    }
}
