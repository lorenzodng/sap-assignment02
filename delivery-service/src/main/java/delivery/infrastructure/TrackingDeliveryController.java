package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentManager;
import delivery.application.ShipmentNotFoundException;
import delivery.domain.Position;
import delivery.domain.ShipmentStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import delivery.domain.Shipment;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Adapter
public class TrackingDeliveryController {

    private static final Logger log = LoggerFactory.getLogger(TrackingDeliveryController.class);
    private final ShipmentManager shipmentManager;

    public TrackingDeliveryController(ShipmentManager shipmentManager) {
        this.shipmentManager = shipmentManager;
    }

    public void registerRoutes(Router router) {
        router.get("/shipments/:id/status").handler(this::getShipmentStatus);
        router.get("/shipments/:id/position").handler(this::getDronePosition);
        router.get("/shipments/:id/remaining-time").handler(this::getRemainingTime);
    }

    private void getShipmentStatus(RoutingContext ctx) {
        try {
            String id = ctx.pathParam("id");
            shipmentManager.checkAndCompleteShipment(id);
            Shipment shipment = shipmentManager.getShipmentDetails(id);
            ShipmentStatus status = shipment.updateStatus();
            log.info("Shipment {} status: {}", id, status.name());

            JSONObject response = new JSONObject();
            response.put("id", id);
            response.put("status", status.name());
            if (status == ShipmentStatus.CANCELLED) {
                response.put("message", "No drone available for this shipment");
            }
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(response.toString());
        } catch (ShipmentNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        } catch (Exception e) {
            ctx.response().setStatusCode(500).end("Internal Server Error");
        }
    }

    private void getDronePosition(RoutingContext ctx) {
        try {
            String id = ctx.pathParam("id");
            shipmentManager.checkAndCompleteShipment(id);
            Shipment shipment = shipmentManager.getShipmentDetails(id);
            Position currentPosition = shipment.calculateCurrentDronePosition();
            if (currentPosition != null) {
                log.info("Delivery {} drone position: {}, {}", id, currentPosition.getLatitude(), currentPosition.getLongitude());

                JSONObject response = new JSONObject();
                response.put("id", id);
                response.put("latitude", currentPosition.getLatitude());
                response.put("longitude", currentPosition.getLongitude());
                ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(response.toString());
            } else {
                ctx.response().setStatusCode(400).end("Position not available: Shipment cancelled or not started");
            }
        } catch (ShipmentNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        } catch (Exception e) {
            ctx.response().setStatusCode(500).end("Internal Server Error");
        }
    }

    private void getRemainingTime(RoutingContext ctx) {
        try {
            String id = ctx.pathParam("id");
            shipmentManager.checkAndCompleteShipment(id);
            Shipment shipment = shipmentManager.getShipmentDetails(id);
            double remainingMinutes = shipment.calculateRemainingTime();
            log.info("Delivery {} remaining time: {} minutes", id, (int) remainingMinutes);

            JSONObject response = new JSONObject();
            response.put("id", id);
            response.put("remainingMinutes", remainingMinutes);
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(response.toString());
        } catch (ShipmentNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error for shipment {}", ctx.pathParam("id"), e);
            ctx.response().setStatusCode(500).end("Internal Server Error");
        }
    }
}