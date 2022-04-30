package com.unrealdinnerbone.apollostats.web;

import com.unrealdinnerbone.apollostats.Stats;
import com.unrealdinnerbone.apollostats.web.ApolloRole;
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WebAccessManger implements AccessManager {

    @Override
    public void manage(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<RouteRole> routeRoles) throws Exception {
        ApolloRole userRole = getUserRole(ctx);
        if (routeRoles.isEmpty() || routeRoles.contains(userRole)) {
            handler.handle(ctx);
        } else {
            ctx.status(401).result(Results.message("You are not authorized to access this resource"));
        }
    }

    ApolloRole getUserRole(Context ctx) {
        String credentials = ctx.header("Password");
        if(credentials != null) {
            if(credentials.equals(Stats.CONFIG.getPushApiKey())) {
                return ApolloRole.POST_API;
            }else {
                return ApolloRole.EVERYONE;
            }
        }else {
            return ApolloRole.EVERYONE;
        }
    }


}
