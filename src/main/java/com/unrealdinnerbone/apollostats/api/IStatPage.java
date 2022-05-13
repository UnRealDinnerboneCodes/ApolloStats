package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.mangers.MatchManger;
import com.unrealdinnerbone.apollostats.mangers.StaffManager;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IStatPage extends IWebPage{

    String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper);

    //Todo add host query and caching
    @Override
    default void getPage(Context handler) {
        Map<Staff, List<Match>> hostMap = MatchManger.getMap();
        String host = handler.queryParam("hosts");
        if(host != null) {
            String[] hosts = host.split(",");
            Map<Staff, List<Match>> map = new HashMap<>();
            Arrays.stream(hosts).forEach(hostName -> StaffManager.findStaff(hostName).ifPresent(staff -> map.put(staff, hostMap.get(staff))));
            hostMap.clear();
            hostMap.putAll(map);
        }


        handler.contentType(getContentType()).result(generateStats(hostMap, ICTXWrapper.of(handler)));
    }

    default ContentType getContentType() {
        return ContentType.TEXT_HTML;
    }

}
