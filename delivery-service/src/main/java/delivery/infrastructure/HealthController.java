package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@Adapter
public class HealthController {

    public void registerRoutes(Router router) {
        router.get("/health").handler(this::healthCheck);
    }

    private void healthCheck(RoutingContext ctx) {
        JsonObject reply = new JsonObject();
        reply.put("status", "UP");
        ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(reply.encode());
    }
}
