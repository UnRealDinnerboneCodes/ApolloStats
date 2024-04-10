package com.unrealdinnerbone.apollo.core.api.event;

import com.unrealdinnerbone.apollo.core.api.Scenario;

import java.util.List;

public record UnknownScenarioEvent(String unknownScen, List<Scenario> guessed) implements IEvent {}
