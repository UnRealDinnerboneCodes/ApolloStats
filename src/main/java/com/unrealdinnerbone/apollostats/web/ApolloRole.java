package com.unrealdinnerbone.apollostats.web;


import io.javalin.security.RouteRole;

public enum ApolloRole implements RouteRole {
    EVERYONE,
    POST_API
    ;
}
