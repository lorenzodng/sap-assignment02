package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DeliveryServiceNotifier;
import drone.domain.Drone;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.buffer.Buffer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Adapter
public class DeliveryServiceClient implements DeliveryServiceNotifier {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceClient.class);
    private final WebClient client;
    private final String deliveryServiceUrl;
    private final OpenTelemetry openTelemetry;
    private static final TextMapSetter<HttpRequest<Buffer>> SETTER = (carrier, key, value) -> carrier.putHeader(key, value);

    public DeliveryServiceClient(Vertx vertx, String deliveryServiceUrl, OpenTelemetry openTelemetry) {
        this.client = WebClient.create(vertx);
        this.deliveryServiceUrl = deliveryServiceUrl;
        this.openTelemetry = openTelemetry;
    }

    @Override
    public Future<Void> notifyDroneAssigned(String shipmentId, Drone drone, double pickupLatitude, double pickupLongitude, double deliveryLatitude, double deliveryLongitude) {

        JSONObject body = new JSONObject();
        body.put("assigned", true);
        body.put("droneId", drone.getId());
        body.put("droneSpeed", Drone.SPEED);
        body.put("droneLatitude", drone.getPosition().getLatitude());
        body.put("droneLongitude", drone.getPosition().getLongitude());
        body.put("pickupLatitude", pickupLatitude);
        body.put("pickupLongitude", pickupLongitude);
        body.put("deliveryLatitude", deliveryLatitude);
        body.put("deliveryLongitude", deliveryLongitude);
        body.put("assignedAt", System.currentTimeMillis());

        HttpRequest<Buffer> request = client.putAbs(deliveryServiceUrl + "/shipments/" + shipmentId + "/assignment").putHeader("Content-Type", "application/json");
        Context otelContext = Vertx.currentContext().get("otelContext");
        openTelemetry.getPropagators().getTextMapPropagator().inject(otelContext, request, SETTER);
        return request.sendBuffer(Buffer.buffer(body.toString()))
                .compose(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return Future.succeededFuture();
                    } else {
                        return Future.failedFuture(new DeliveryServiceException("Delivery service error: " + response.statusCode()));
                    }
                })
                .onSuccess(res -> {
                    log.info("Drone {} assigned to shipment {}", drone.getId(), shipmentId);
                    log.info("Shipment {} drone assigned notified", shipmentId);
                })
                .onFailure(err -> log.error("Failed to notify delivery service for shipment {}", shipmentId, err))
                .mapEmpty();
    }

    @Override
    public Future<Void> notifyDroneNotAvailable(String shipmentId) {

        JSONObject body = new JSONObject();
        body.put("assigned", false);

        HttpRequest<Buffer> request = client.putAbs(deliveryServiceUrl + "/shipments/" + shipmentId + "/assignment").putHeader("Content-Type", "application/json");
        Context otelContext = Vertx.currentContext().get("otelContext");
        openTelemetry.getPropagators().getTextMapPropagator().inject(otelContext, request, SETTER);
        return request.sendBuffer(Buffer.buffer(body.toString()))
                .compose(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return Future.succeededFuture();
                    } else {
                        return Future.failedFuture(new DeliveryServiceException("Delivery service error: " + response.statusCode()));
                    }
                })
                .onSuccess(res -> {
                    log.warn("No available drones for shipment {}", shipmentId);
                    log.warn("Shipment {} drone not available notified", shipmentId);
                })
                .onFailure(err -> log.error("Failed to notify delivery service for shipment {}", shipmentId, err))
                .mapEmpty();
    }
}