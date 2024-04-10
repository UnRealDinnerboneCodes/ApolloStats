package com.unrealdinnerbone.apollo.web.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICTXGetter {
    Optional<String> queryParam(String param);

    String pathParam(String param);

    String getRequestID();

    Map<String, List<String>> getQueryPerms();

}
