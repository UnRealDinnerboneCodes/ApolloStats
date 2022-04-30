package com.unrealdinnerbone.apollostats.web;

import io.javalin.core.security.RouteRole;

public enum ApolloRole implements RouteRole {
    EVERYONE,
    POST_API
    ;
}
