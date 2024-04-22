package com.unrealdinnerbone.apollo.core.stats;

import com.unrealdinnerbone.apollo.core.api.Match;

import java.util.List;

public interface IStatType<T>
{
    T getStat(List<Match> matches);
}
