package drone.application;

import drone.domain.Drone;
import drone.domain.GeoUtils;
import java.util.List;

public class AssignDroneImpl implements AssignDrone {

    private final CheckDroneAvailability checkDroneAvailability;

    public AssignDroneImpl(CheckDroneAvailability checkDroneAvailability) {
        this.checkDroneAvailability = checkDroneAvailability;
    }

    @Override
    public Drone assign(List<Drone> drones, double packageWeight, double pickupLatitude, double pickupLongitude, double distancePickupToDelivery, int deliveryTimeLimit) {
        return drones.stream().filter(Drone::isAvailable).filter(drone -> {
            double distanceDroneToPickup = GeoUtils.haversine(drone.getPosition().getLatitude(), drone.getPosition().getLongitude(), pickupLatitude, pickupLongitude);
            return checkDroneAvailability.check(drone, packageWeight, distanceDroneToPickup, distancePickupToDelivery, deliveryTimeLimit);
        }).min((d1, d2) -> {
            double dist1 = GeoUtils.haversine(d1.getPosition().getLatitude(), d1.getPosition().getLongitude(), pickupLatitude, pickupLongitude);
            double dist2 = GeoUtils.haversine(d2.getPosition().getLatitude(), d2.getPosition().getLongitude(), pickupLatitude, pickupLongitude);
            return Double.compare(dist1, dist2);
        }).orElseThrow(DroneNotAvailableException::new);
    }
}