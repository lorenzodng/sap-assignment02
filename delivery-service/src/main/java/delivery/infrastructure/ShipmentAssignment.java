package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.domain.Position;
import delivery.domain.Shipment;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

//controller che riceve le notifiche di assegnazione drone da drone-service
@Adapter
public class ShipmentAssignment {

    private static final Logger log = LoggerFactory.getLogger(ShipmentAssignment.class);
    private final Map<String, Shipment> shipments; //mappa che tiene traccia dei tutte le spedizioni

    public ShipmentAssignment(Map<String, Shipment> shipments) {
        this.shipments = shipments;
    }

    //rotte
    public void registerRoutes(Router router) {
        router.put("/shipments/:id/assignment").handler(BodyHandler.create()).handler(this::handleShipmentAssignment);
    }

    //gestisce la creazione della spedizione in base all'assegnazione del drone
    private void handleShipmentAssignment(RoutingContext ctx) {
        String shipmentId = ctx.pathParam("id");
        JSONObject body = new JSONObject(ctx.body().asString()); //recupera il body
        boolean assigned = body.getBoolean("assigned"); //recupera il contenuto

        if (assigned) { //se è true
            log.info("Shipment {} drone assigned received", shipmentId);
            Position droneInitialPosition = new Position(body.getDouble("droneLatitude"), body.getDouble("droneLongitude"));
            Position pickupPosition = new Position(body.getDouble("pickupLatitude"), body.getDouble("pickupLongitude"));
            Position deliveryPosition = new Position(body.getDouble("deliveryLatitude"), body.getDouble("deliveryLongitude"));
            long assignedAt = body.getLong("assignedAt");
            double droneSpeed = body.getDouble("droneSpeed");
            Shipment shipment = new Shipment(shipmentId, droneInitialPosition, pickupPosition, deliveryPosition, assignedAt, droneSpeed); //crea la spedizione con stato "scheduled"
            shipments.put(shipmentId, shipment); //aggiunge la spedizione alla mappa
            log.info("Shipment {} scheduled", shipmentId);
            ctx.response().setStatusCode(200).end();
        } else { //se è false
            log.info("Shipment {} drone not available received", shipmentId);
            Shipment shipment = new Shipment(shipmentId); //crea la spedizione con stato "cancelled"
            shipments.put(shipmentId, shipment); //aggiunge la spedizione alla mappa
            log.info("Shipment {} cancelled", shipmentId);
            ctx.response().setStatusCode(200).end();
        }
    }
}