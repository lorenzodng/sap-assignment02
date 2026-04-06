package drone.application;

import buildingblocks.application.InboundPort;
import drone.domain.Drone;

import java.util.List;

@InboundPort
public interface AssignDrone {
    Drone assign(List<Drone> drones, double packageWeight, double pickupLatitude, double pickupLongitude, double distancePickupToDelivery, int deliveryTimeLimit);
}