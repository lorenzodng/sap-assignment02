package drone.application;

import buildingblocks.application.InboundPort;
import io.vertx.core.Future;

@InboundPort
public interface DroneAssignmentOrchestrator {
    Future<Void> orchestrateAssignment(String shipmentId, double pickupLat, double pickupLon, double deliveryLat, double deliveryLon, double weight, int timeLimit);
}