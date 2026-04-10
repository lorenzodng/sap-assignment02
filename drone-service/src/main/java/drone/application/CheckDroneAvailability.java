package drone.application;

import drone.domain.Drone;

public interface CheckDroneAvailability {
    boolean check(Drone drone, double packageWeight, double distanceDroneToPickup, double distancePickupToDelivery, int deliveryTimeLimit);
}