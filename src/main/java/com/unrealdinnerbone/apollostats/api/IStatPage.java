package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.mangers.MatchManger;
import com.unrealdinnerbone.apollostats.mangers.StaffManager;
import io.javalin.http.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String year = handler.queryParam("year");
        if(year != null) {
            Map<Staff, List<Match>> map = new HashMap<>();
            for (Map.Entry<Staff, List<Match>> staffListEntry : hostMap.entrySet()) {
                List<Match> matches = staffListEntry.getValue();
                matches.removeIf(match -> !match.opens().split("-")[0].equals(year));
                map.put(staffListEntry.getKey(), matches);
            }
            hostMap.clear();
            hostMap.putAll(map);
        }

        generateStats(hostMap, ICTXWrapper.of(handler));
    }


}
