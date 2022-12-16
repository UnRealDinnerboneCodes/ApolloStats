package com.unrealdinnerbone.apollostats.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unrealdinnerbone.apollostats.mangers.MatchManger;
import com.unrealdinnerbone.apollostats.mangers.StaffManager;
import io.javalin.http.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface IStatPage extends IWebPage{

    void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper);

    //Todo add caching
    @Override
    default void getPage(Context handler) {
        Map<Staff, List<Match>> hostMap = new HashMap<>(MatchManger.getMap());
        String host = handler.queryParam("hosts");
        if(host != null) {
            String[] hosts = host.split(",");
            Map<Staff, List<Match>> map = new HashMap<>();
            Arrays.stream(hosts).forEach(hostName -> StaffManager.findStaff(hostName).ifPresent(staff -> map.put(staff, hostMap.get(staff))));
            hostMap.clear();
            hostMap.putAll(map);
        }

        generateStats(hostMap, ICTXWrapper.of(handler));
    }


}
