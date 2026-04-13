package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//controller che riceve le notifiche di assegnazione drone da drone-service
@Adapter
public class ShipmentAssignmentController {

    private static final Logger log = LoggerFactory.getLogger(ShipmentAssignmentController.class);
    private final ShipmentManager shipmentManager;

    public ShipmentAssignmentController(ShipmentManager shipmentManager) {
        this.shipmentManager = shipmentManager;
    }

    //rotte
    public void registerRoutes(Router router) {
        router.put("/shipments/:id/assignment").handler(BodyHandler.create()).handler(this::handleShipmentAssignment);
    }

    //gestisce la creazione della spedizione in base all'assegnazione del drone
    private void handleShipmentAssignment(RoutingContext ctx) {
        try {
            String shipmentId = ctx.pathParam("id");
            JSONObject body = new JSONObject(ctx.body().asString()); //recupera il body
            boolean assigned = body.getBoolean("assigned"); //recupera il contenuto

            if (assigned) { //se è true
                log.info("Shipment {} drone assigned received", shipmentId);
                shipmentManager.createShipmentFromAssignment(shipmentId, true, body.getDouble("droneLatitude"), body.getDouble("droneLongitude"), body.getDouble("pickupLatitude"), body.getDouble("pickupLongitude"), body.getDouble("deliveryLatitude"), body.getDouble("deliveryLongitude"), body.getLong("assignedAt"), body.getDouble("droneSpeed")); //crea la spedizione con stato "scheduled"
            } else { //se è false
                log.info("Shipment {} drone not available received", shipmentId);
                shipmentManager.createShipmentFromAssignment(shipmentId, false, null, null, null, null, null, null, 0L, 0.0); //crea la spedizione con stato "cancelled"
            }
            ctx.response().setStatusCode(200).end();
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid assignment data");
        }
    }
}