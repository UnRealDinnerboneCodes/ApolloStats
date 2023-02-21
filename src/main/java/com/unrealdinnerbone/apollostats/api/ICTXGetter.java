package com.unrealdinnerbone.apollostats.api;

import java.util.Optional;

public interface ICTXGetter {
    Optional<String> queryParam(String param);

    String pathParam(String param);

}
