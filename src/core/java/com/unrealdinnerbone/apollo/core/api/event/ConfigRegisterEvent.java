package com.unrealdinnerbone.apollo.core.api.event;

import com.unrealdinnerbone.config.impl.provider.EnvProvider;

public record ConfigRegisterEvent(EnvProvider envProvider) implements IEvent {

}
