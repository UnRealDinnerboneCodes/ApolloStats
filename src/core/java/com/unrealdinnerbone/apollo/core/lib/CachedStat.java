package com.unrealdinnerbone.apollo.core.lib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unrealdinnerbone.apollo.core.api.Match;
import com.unrealdinnerbone.apollo.core.api.Staff;
import com.unrealdinnerbone.unreallib.list.LazyHashMap;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CachedStat<T> {

    private static final List<CachedStat<?>> cachedStats = new ArrayList<>();
    private final TriFunction<String, Staff, List<Match>, T> function;

    private final LazyHashMap<Staff, Cache<String, T>> cache;

    public CachedStat(TriFunction<String, Staff, List<Match>, T> function) {
        this.function = function;
        this.cache = new LazyHashMap<>(s -> CacheBuilder.newBuilder()
                .build());
        cachedStats.add(this);
    }

    public T get(String name, Staff staff, List<Match> matches) {
        try {
            return cache.get(staff).get(name, () -> function.apply(name, staff, new ArrayList<>(matches)));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear(Staff staff) {
        cache.ifPresent(staff, Cache::invalidateAll);
        cache.remove(staff);
    }

    public static List<CachedStat<?>> getCachedStats() {
        return cachedStats;
    }
}
