package com.unrealdinnerbone.apollo.core;

import com.unrealdinnerbone.apollo.core.api.event.IEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ApolloEventManager<T>
{
    public static final ApolloEventManager<IEvent> EVENT_MANAGER = new ApolloEventManager<>();

    private final ConcurrentHashMap<Class<? extends T>, Queue<Consumer<T>>> interactions = new ConcurrentHashMap<>();

    public <B extends T> void registerHandler(Class<B> eventClass, Consumer<B> eventConsumer) {
        if (!interactions.containsKey(eventClass)) {
            interactions.put(eventClass, new ConcurrentLinkedQueue<>());
        }
        interactions.get(eventClass).add((Consumer<T>) eventConsumer);
    }

    public void post(T eClass) {
        Class<T> eventClass = (Class<T>) eClass.getClass();
        if (interactions.containsKey(eventClass)) {
            interactions.get(eventClass).forEach(runnable -> runnable.accept(eClass));
        }
    }
}
