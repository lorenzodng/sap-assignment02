package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Adapter
public class ShipmentAssignmentController {

    private static final Logger log = LoggerFactory.getLogger(ShipmentAssignmentController.class);
    private final ShipmentManager shipmentManager;

    public ShipmentAssignmentController(ShipmentManager shipmentManager) {
        this.shipmentManager = shipmentManager;
    }

    public void registerRoutes(Router router) {
        router.put("/shipments/:id/assignment").handler(BodyHandler.create()).handler(this::handleShipmentAssignment);
    }

    private void handleShipmentAssignment(RoutingContext ctx) {
        try {
            String shipmentId = ctx.pathParam("id");
            JSONObject body = new JSONObject(ctx.body().asString());
            boolean assigned = body.getBoolean("assigned");
            if (assigned) {
                log.info("Shipment {} drone assigned received", shipmentId);
                shipmentManager.createShipmentFromAssignment(shipmentId, true, body.getDouble("droneLatitude"), body.getDouble("droneLongitude"), body.getDouble("pickupLatitude"), body.getDouble("pickupLongitude"), body.getDouble("deliveryLatitude"), body.getDouble("deliveryLongitude"), body.getLong("assignedAt"), body.getDouble("droneSpeed"));
            } else {
                log.info("Shipment {} drone not available received", shipmentId);
                shipmentManager.createShipmentFromAssignment(shipmentId, false, null, null, null, null, null, null, 0L, 0.0);
            }
            ctx.response().setStatusCode(200).end();
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid assignment data");
        }
    }
}