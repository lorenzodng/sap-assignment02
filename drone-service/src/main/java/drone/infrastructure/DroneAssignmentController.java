package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.AssignDrone;
import drone.domain.Drone;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

//controller che riceve le notifiche di richiesta creata da request-service
@Adapter
public class DroneAssignmentController {

    private static final Logger log = LoggerFactory.getLogger(DroneAssignmentController.class);
    private final AssignDrone assignDrone;
    private final List<Drone> drones; //lista dei droni disponibili
    private final DeliveryServiceClient deliveryServiceClient;

    public DroneAssignmentController(AssignDrone assignDrone, List<Drone> drones, DeliveryServiceClient deliveryServiceClient) {
        this.assignDrone = assignDrone;
        this.drones = drones;
        this.deliveryServiceClient = deliveryServiceClient;
    }

    //rotte
    public void registerRoutes(Router router) {
        router.post("/shipments/assign").handler(BodyHandler.create()).handler(this::assignDroneToShipment);
    }

    //assegna un drone alla spedizione
    private void assignDroneToShipment(RoutingContext ctx) {
        JSONObject body = new JSONObject(ctx.body().asString()); //recupera il body dal messaggio
        String shipmentId = body.getString("shipmentId");
        log.info("Shipment {} request received", shipmentId);
        double pickupLatitude = body.getDouble("pickupLatitude");
        double pickupLongitude = body.getDouble("pickupLongitude");
        double deliveryLatitude = body.getDouble("deliveryLatitude");
        double deliveryLongitude = body.getDouble("deliveryLongitude");
        double packageWeight = body.getDouble("packageWeight");
        int deliveryTimeLimit = body.getInt("deliveryTimeLimit");
        double distancePickupToDelivery = calculateDistance(pickupLatitude, pickupLongitude, deliveryLatitude, deliveryLongitude);
        Drone assignedDrone = assignDrone.assign(drones, packageWeight, pickupLatitude, pickupLongitude, distancePickupToDelivery, deliveryTimeLimit);
        if (assignedDrone != null) {
            assignedDrone.setAvailable(false);
            deliveryServiceClient.notifyDroneAssigned(shipmentId, assignedDrone, pickupLatitude, pickupLongitude, deliveryLatitude, deliveryLongitude).onSuccess(response -> {
                ctx.response().setStatusCode(201).end();
            }).onFailure(err -> {
                ctx.response().setStatusCode(500).end("Error contacting drone service");
            });
        } else {
            deliveryServiceClient.notifyDroneNotAvailable(shipmentId).onSuccess(res -> {
                ctx.response().setStatusCode(503).end("No drone available");
            }).onFailure(err -> {
                ctx.response().setStatusCode(500).end("Error contacting delivery service");
            });
        }
    }

    //calcola la distanza tra la base del drone al punto di pickup
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDiff = lat1 - lat2;
        double lonDiff = lon1 - lon2;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }
}