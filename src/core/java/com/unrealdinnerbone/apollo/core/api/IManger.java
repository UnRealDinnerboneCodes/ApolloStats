package com.unrealdinnerbone.apollo.core.api;

import java.util.concurrent.CompletableFuture;

public interface IManger {
    CompletableFuture<Void> start();

    String getName();
}
