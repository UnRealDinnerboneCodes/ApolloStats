package com.unrealdinnerbone.apollo.core.api.event;

import com.unrealdinnerbone.apollo.core.api.Game;
import com.unrealdinnerbone.apollo.core.api.Match;

public interface MatchEvents extends IEvent {

    Match match();

    interface GameMatchEvent extends MatchEvents {
        Game game();
    }


    record GameFound(Match match) implements MatchEvents {}

    record GameRemoved(Match match) implements MatchEvents {}

    record GameSaved(Match match, Game game) implements GameMatchEvent {}

}
