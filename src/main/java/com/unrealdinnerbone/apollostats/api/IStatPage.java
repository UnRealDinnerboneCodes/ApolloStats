package com.unrealdinnerbone.apollostats.api;

import com.unrealdinnerbone.apollostats.mangers.MatchManger;
import com.unrealdinnerbone.apollostats.web.ApolloRole;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IStatPage extends IWebPage{

    String generateStats(Map<Staff, List<Match>> hostMatchMap, ICTXWrapper wrapper);

    //Todo add host query and caching
    @Override
    default void getPage(Context handler) {
        handler.contentType(getContentType()).result(generateStats(MatchManger.getMap(), ICTXWrapper.of(handler)));
    }

    default ContentType getContentType() {
        return ContentType.TEXT_HTML;
    }

}
