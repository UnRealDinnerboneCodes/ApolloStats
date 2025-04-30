package com.unrealdinnerbone.apollo.web.instacnes;

import com.unrealdinnerbone.apollo.core.Stats;
import com.unrealdinnerbone.apollo.core.mangers.SharkbobTimerManger;
import com.unrealdinnerbone.apollo.web.api.IWebPage;
import com.unrealdinnerbone.apollo.web.api.WebInstance;
import com.unrealdinnerbone.unreallib.json.JsonUtil;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

public class SharkbobInstance extends WebInstance<IWebPage> {

    public SharkbobInstance() {
        super(4165, javalinConfig -> {});
    }

    @Override
    public Map<Type, List<IWebPage>> getPages() {
        return Map.of(Type.POST, List.of(new API.Reset()),
                Type.GET, List.of(new API.Status()));
    }


    public static class API {

        public static class Reset implements IWebPage {

            @Override
            public void getPage(Context handler) {
                String body = handler.body();
                try {
                    ResetMessage parse = JsonUtil.DEFAULT.parse(ResetMessage.class, body);
                    if (parse.user().isEmpty() || parse.reason().isEmpty()) {
                        handler.status(400);
                        handler.result("{\"error\": \"Invalid JSON\"}");
                        return;
                    }
                    SharkbobTimerManger sharkbobTimerManger = Stats.INSTANCE.getSharkbobTimerManger();
                    long lastReset = sharkbobTimerManger.getSharkbobResets().keySet().stream().max(Long::compareTo).orElse(0L);
                    sharkbobTimerManger.addSharkbobReset(new SharkbobTimerManger.SharkbobReset(System.currentTimeMillis(), parse.reason(), parse.user()));
                    handler.status(200);
                    handler.result("{\"message\": \"Reset added successfully\", \"lastReset\": " + lastReset + "}");
                }catch (Exception e) {
                    handler.status(400);
                    handler.result("{\"error\": \"Invalid JSON\"}");
                    return;
                }
            }

            @Override
            public String getPath() {
                return "api/v1/reset";
            }

            public record ResetMessage(String user, String reason) {
            }
        }

        public static class Status implements IWebPage {

            @Override
            public void getPage(Context handler) {
                SharkbobTimerManger timer = Stats.INSTANCE.getSharkbobTimerManger();
                handler.status(200);
                handler.result("{\"lastReset\": " + timer.getSharkbobResets().keySet().stream().max(Long::compareTo).orElse(0L) + ", \"resets\": " + timer.getSharkbobResets().size() + "}");
            }

            @Override
            public String getPath() {
                return "api/v1/status";
            }
        }
    }
}
