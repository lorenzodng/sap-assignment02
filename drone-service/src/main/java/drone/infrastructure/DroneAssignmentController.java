package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DroneAssignmentOrchestrator;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.json.JSONObject;

//controller che riceve le notifiche di richiesta creata da request-service
@Adapter
public class DroneAssignmentController {

    private final DroneAssignmentOrchestrator orchestrator;

    public DroneAssignmentController(DroneAssignmentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    //rotte
    public void registerRoutes(Router router) {
        router.post("/shipments/assign").handler(BodyHandler.create()).handler(this::assignDroneToShipment);
    }

    //assegna un drone alla spedizione
    private void assignDroneToShipment(RoutingContext ctx) {
        JSONObject body = new JSONObject(ctx.body().asString()); //recupera il body dal messaggio
        String shipmentId = body.getString("shipmentId");

        orchestrator.orchestrateAssignment(shipmentId, body.getDouble("pickupLatitude"), body.getDouble("pickupLongitude"), body.getDouble("deliveryLatitude"), body.getDouble("deliveryLongitude"), body.getDouble("packageWeight"), body.getInt("deliveryTimeLimit"))
                .onSuccess(v -> ctx.response().setStatusCode(201).end())
                .onFailure(err -> {
                    if ("NO_DRONE_AVAILABLE".equals(err.getMessage())) {
                        ctx.response().setStatusCode(503).end("No drone available");
                    } else {
                        ctx.response().setStatusCode(500).end("Internal Server Error");
                    }
                });
    }
}