package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

//verifica lo stato di salute di tutti i microservizi nel sistema
@Adapter
public class HealthCheckerController {

    private final WebClient client;
    private final String requestServiceUrl;
    private final String droneServiceUrl;
    private final String deliveryServiceUrl;

    public HealthCheckerController(Vertx vertx, String requestServiceUrl, String droneServiceUrl, String deliveryServiceUrl) {
        this.client = WebClient.create(vertx);
        this.requestServiceUrl = requestServiceUrl;
        this.droneServiceUrl = droneServiceUrl;
        this.deliveryServiceUrl = deliveryServiceUrl;
    }

    public void registerRoutes(Router router) {
        router.get("/health").handler(this::healthCheckHandler);
    }

    //interroga i microservizi per richiedere lo stato di salute
    private void healthCheckHandler(RoutingContext ctx) {

        //vengono utilizzate future perchè è necessario recuperare lo stato di tutti i microservizi, quindi è necessario attendere che tutte le chiamate siano completate?

        Future<HttpResponse<Buffer>> reqFut = client.getAbs(requestServiceUrl + "/health").send().recover(e -> Future.succeededFuture(null));
        Future<HttpResponse<Buffer>> droneFut = client.getAbs(droneServiceUrl + "/health").send().recover(e -> Future.succeededFuture(null));
        Future<HttpResponse<Buffer>> delFut = client.getAbs(deliveryServiceUrl + "/health").send().recover(e -> Future.succeededFuture(null));

        //una volta che tutti i risultati delle chiamate sono stati recuperati...
        Future.all(reqFut, droneFut, delFut).onSuccess(results -> {
            JsonObject checks = new JsonObject();

            checks.put("request-service", isServiceUp(results.resultAt(0)));
            checks.put("drone-service", isServiceUp(results.resultAt(1)));
            checks.put("delivery-service", isServiceUp(results.resultAt(2)));

            JsonObject reply = new JsonObject();
            reply.put("status", "UP");
            reply.put("checks", checks);
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(reply.encode());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Health check aggregation failed");
        });
    }

    private String isServiceUp(HttpResponse<Buffer> response) {
        return (response != null && response.statusCode() == 200) ? "UP" : "DOWN";
    }
}