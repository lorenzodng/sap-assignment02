package drone.application;

import buildingblocks.application.OutboundPort;
import io.vertx.core.Future;
import drone.domain.Drone;

@OutboundPort
public interface DeliveryServiceNotifier {
    Future<Void> notifyDroneAssigned(String shipmentId, Drone drone, double pickupLat, double pickupLong, double deliveryLat, double deliveryLong);
    Future<Void> notifyDroneNotAvailable(String shipmentId);
}