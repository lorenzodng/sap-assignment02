package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import gateway.application.ApiGatewayMetrics;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

@Adapter
public class ApiGatewayController {

    private final WebClient client;
    private final String requestServiceUrl;
    private final String deliveryServiceUrl;
    private final ApiGatewayMetrics metrics;

    public ApiGatewayController(Vertx vertx, String requestServiceUrl, String deliveryServiceUrl, ApiGatewayMetrics metrics) {
        this.client = WebClient.create(vertx);
        this.requestServiceUrl = requestServiceUrl;
        this.deliveryServiceUrl = deliveryServiceUrl;
        this.metrics = metrics;
    }

    //registra le rotte che il client può richiamare
    public void registerRoutes(Router router) {
        router.post("/shipments").handler(BodyHandler.create()).handler(this::createShipment);
        router.get("/shipments/:id/status").handler(this::getShipmentStatus);
        router.get("/shipments/:id/position").handler(this::getDronePosition);
        router.get("/shipments/:id/remaining-time").handler(this::getRemainingTime);
    }

    //inoltra la richiesta di creazione spedizione a request-management
    private void createShipment(RoutingContext ctx) {
        String path = "/shipments";
        String method = "POST";
        /*
        1) crea una richiesta post verso il microservizio request-management a uno specifico indirizzo
        2) se la chiamata ha successo, recupera il codice di stato e invia al client il body come stringa
        3) se fallisce, invia al client un messaggio di errore
         */
        client.postAbs(requestServiceUrl + "/shipments").sendBuffer(Buffer.buffer(ctx.body().asString()))
                .onSuccess(response -> {
                    metrics.incrementRequest(path, method, response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
                }).onFailure(err -> {
                    metrics.incrementRequest(path, method, 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }

    //inoltra la richiesta di tracking a delivery-management
    private void getShipmentStatus(RoutingContext ctx) {
        String path = "/shipments/:id/status";
        String method = "GET";
        String id = ctx.pathParam("id");
        client.getAbs(deliveryServiceUrl + "/shipments/" + id + "/status").send()
                .onSuccess(response -> {
                    metrics.incrementRequest(path, method, response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).putHeader("Content-Type", "application/json").end(response.bodyAsString());
                }).onFailure(err -> {
                    metrics.incrementRequest(path, method, 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }

    //inoltra la richiesta di posizione a delivery-management
    private void getDronePosition(RoutingContext ctx) {
        String path = "/shipments/:id/position"; // Definisci il path template
        String method = "GET";
        String id = ctx.pathParam("id");
        client.getAbs(deliveryServiceUrl + "/shipments/" + id + "/position").send()
                .onSuccess(response -> {
                    metrics.incrementRequest(path, method, response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).putHeader("Content-Type", "application/json").end(response.bodyAsString());
                }).onFailure(err -> {
                    metrics.incrementRequest(path, method, 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }

    //inoltra la richiesta di tempo rimanente a delivery-management
    private void getRemainingTime(RoutingContext ctx) {
        String path = "/shipments/:id/remaining-time"; // Definisci il path template
        String method = "GET";
        String id = ctx.pathParam("id");
        client.getAbs(deliveryServiceUrl + "/shipments/" + id + "/remaining-time").send()
                .onSuccess(response -> {
                    metrics.incrementRequest(path, method, response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).putHeader("Content-Type", "application/json").end(response.bodyAsString());
                }).onFailure(err -> {
                    metrics.incrementRequest(path, method, 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }
}