package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import gateway.application.ApiGatewayObserver;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.ArrayList;

@Adapter
public class ApiGatewayController {

    private final WebClient client;
    private final String requestServiceUrl;
    private final String deliveryServiceUrl;
    private final ArrayList<ApiGatewayObserver> observers = new ArrayList<>();

    public ApiGatewayController(Vertx vertx, String requestServiceUrl, String deliveryServiceUrl) {
        this.client = WebClient.create(vertx);
        this.requestServiceUrl = requestServiceUrl;
        this.deliveryServiceUrl = deliveryServiceUrl;
    }

    //osservatori
    public void addObserver(ApiGatewayObserver observer) {
        observers.add(observer);
    }

    private void notifyShipmentRequest() {
        observers.forEach(ApiGatewayObserver::notifyShipmentRequest);
    }

    private void notifyTrackingRequest() {
        observers.forEach(ApiGatewayObserver::notifyTrackingRequest);
    }

    private void notifyValidShipmentRequest() {
        observers.forEach(ApiGatewayObserver::notifyValidShipmentRequest);
    }

    private void notifyDroneAvailable() {
        observers.forEach(ApiGatewayObserver::notifyDroneAvailable);
    }

    private void notifyDroneNotAvailable() {
        observers.forEach(ApiGatewayObserver::notifyDroneNotAvailable);
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
        /*
        1) crea una richiesta post verso il microservizio request-management a uno specifico indirizzo
        2) se la chiamata ha successo, recupera il codice di stato e invia al client il body come stringa
        3) se fallisce, invia al client un messaggio di errore
         */
        client.postAbs(requestServiceUrl + "/shipments").sendBuffer(Buffer.buffer(ctx.body().asString())).onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }

    //inoltra la richiesta di tracking a delivery-management
    private void getShipmentStatus(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryServiceUrl + "/shipments/" + id + "/status").send().onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }

    //inoltra la richiesta di posizione a delivery-management
    private void getDronePosition(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryServiceUrl + "/shipments/" + id + "/position").send().onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }

    //inoltra la richiesta di tempo rimanente a delivery-management
    private void getRemainingTime(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryServiceUrl + "/shipments/" + id + "/remaining-time").send().onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }
}