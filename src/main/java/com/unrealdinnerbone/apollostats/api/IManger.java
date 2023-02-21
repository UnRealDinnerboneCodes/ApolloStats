package com.unrealdinnerbone.apollostats.api;

import java.util.concurrent.CompletableFuture;

public interface IManger {
    CompletableFuture<Void> start();

    String getName();
}
