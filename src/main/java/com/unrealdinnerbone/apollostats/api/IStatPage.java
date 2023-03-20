package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.unreallib.exception.WebResultException;
import io.javalin.http.Context;

import java.util.*;
import java.util.stream.Collectors;

public interface IStatPage extends IWebPage{

    void generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper) throws WebResultException;

    default boolean filterMatches(Match match) {
        return match.isGoodGame();
    }
    //Todo add caching
    @Override
    default void getPage(Context handler) {
        Map<Staff, List<Match>> hostMap = new HashMap<>();
        Stats.INSTANCE.getMatchManger().getMap().forEach((key, value) -> hostMap.put(key, value.stream().filter(this::filterMatches).collect(Collectors.toList())));
        String host = handler.queryParam("hosts");
        if(host != null) {
            String[] hosts = host.split(",");
            Map<Staff, List<Match>> map = new HashMap<>();
            Arrays.stream(hosts).forEach(hostName -> Stats.INSTANCE.getStaffManager().findStaff(hostName).ifPresent(staff -> map.put(staff, new ArrayList<>(hostMap.get(staff)))));
            hostMap.clear();
            hostMap.putAll(map);
        }




        try {

            {
                String year = handler.queryParam("year");
                if(year != null) {
                    List<String> years = Arrays.stream(year.split(",")).toList();
                    for (String s : years) {
                        try {
                            Integer.parseInt(s);
                        }catch (NumberFormatException e) {
                            throw new WebResultException("Invalid Year: " + s, 400);
                        }
                    }
                    Map<Staff, List<Match>> map = new HashMap<>();
                    for (Map.Entry<Staff, List<Match>> staffListEntry : hostMap.entrySet()) {
                        List<Match> matches = new ArrayList<>(staffListEntry.getValue());
                        matches.removeIf(match -> !years.contains(match.opens().split("-")[0]));
                        map.put(staffListEntry.getKey(), matches);
                    }
                    hostMap.clear();
                    hostMap.putAll(map);
                }
            }

            {
                String type = handler.queryParam("type");
                if (type != null) {
                    List<Staff.Type> wantedTypes = new ArrayList<>();
                    for (String s : type.split(",")) {
                        Staff.Type foundType = Staff.Type.fromString(s).orElseThrow(() -> new WebResultException("Invalid Type" + type, 400));
                        wantedTypes.add(foundType);
                    }
                    Map<Staff, List<Match>> map = new HashMap<>();
                    hostMap.entrySet()
                            .stream().filter(staffListEntry -> wantedTypes.contains(staffListEntry.getKey().type()))
                            .forEach(staffListEntry -> map.put(staffListEntry.getKey(), staffListEntry.getValue()));
                    hostMap.clear();
                    hostMap.putAll(map);
                }
            }
            hostMap.entrySet().removeIf(staffListEntry -> staffListEntry.getValue().isEmpty());
            generateStats(hostMap, ICTXWrapper.of(handler));
        }catch (WebResultException e) {
            handler.status(e.getCode()).html(Stats.getResourceAsString("error.html")
                    .replace("{Error_Message}", e.getMessage())
                    .replace("{Error_Code}", String.valueOf(e.getCode())));
        }

    }


}
