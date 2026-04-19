package drone.application;

import drone.domain.Drone;

public class CheckDroneAvailabilityImpl implements CheckDroneAvailability {

    @Override
    public boolean check(Drone drone, double packageWeight, double distanceDroneToPickup, double distancePickupToDelivery, int deliveryTimeLimit) {

        if (Drone.WEIGHT_CAPACITY < packageWeight) {
            return false;
        }

        double deliveryTimeMinutes = ((distanceDroneToPickup + distancePickupToDelivery) / Drone.SPEED) * 60;
        if (deliveryTimeMinutes > deliveryTimeLimit) {
            return false;
        }

        double availableBattery = Drone.INITIAL_BATTERY - Drone.SAFETY_BATTERY;
        double requiredBattery = (distanceDroneToPickup + distancePickupToDelivery) * Drone.BATTERY_CONSUMPTION_PER_KM;
        if (availableBattery < requiredBattery) {
            return false;
        }

        return true;
    }
}