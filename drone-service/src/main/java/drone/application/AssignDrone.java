package drone.application;

import drone.domain.Drone;
import java.util.List;

public interface AssignDrone {
    Drone assign(List<Drone> drones, double packageWeight, double pickupLatitude, double pickupLongitude, double distancePickupToDelivery, int deliveryTimeLimit);
}