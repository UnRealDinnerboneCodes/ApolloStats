package com.unrealdinnerbone.apollostats.web;

import com.unrealdinnerbone.apollostats.Stats;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WebAccessManger implements AccessManager {

    ApolloRole getUserRole(Context ctx) {
        String credentials = ctx.header("Password");
        if(credentials != null) {
            if(credentials.equals(Stats.INSTANCE.getStatsConfig().getPushApiKey())) {
                return ApolloRole.POST_API;
            }else {
                return ApolloRole.EVERYONE;
            }
        }else {
            return ApolloRole.EVERYONE;
        }
    }


    @Override
    public void manage(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<? extends RouteRole> routeRoles) throws Exception {
        ApolloRole userRole = getUserRole(ctx);
        if (routeRoles.isEmpty() || routeRoles.contains(userRole)) {
            handler.handle(ctx);
        } else {
            ctx.status(HttpStatus.UNAUTHORIZED).result(Results.message("You are not authorized to access this resource"));
        }
    }
}
