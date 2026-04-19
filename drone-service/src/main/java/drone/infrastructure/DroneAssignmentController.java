package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DroneAssignmentOrchestrator;
import drone.application.DroneNotAvailableException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.json.JSONObject;

@Adapter
public class DroneAssignmentController {

    private final DroneAssignmentOrchestrator orchestrator;

    public DroneAssignmentController(DroneAssignmentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void registerRoutes(Router router) {
        router.post("/shipments/assign").handler(BodyHandler.create()).handler(this::assignDroneToShipment);
    }

    private void assignDroneToShipment(RoutingContext ctx) {
        JSONObject body = new JSONObject(ctx.body().asString());
        String shipmentId = body.getString("shipmentId");

        orchestrator.orchestrateAssignment(shipmentId, body.getDouble("pickupLatitude"), body.getDouble("pickupLongitude"), body.getDouble("deliveryLatitude"), body.getDouble("deliveryLongitude"), body.getDouble("packageWeight"), body.getInt("deliveryTimeLimit"))
                .onSuccess(v -> ctx.response().setStatusCode(201).end())
                .onFailure(err -> {
                    if (err instanceof DroneNotAvailableException) {
                        ctx.response().setStatusCode(503).end("No drone available");
                    } else {
                        ctx.response().setStatusCode(500).end("Internal Server Error");
                    }
                });
    }
}