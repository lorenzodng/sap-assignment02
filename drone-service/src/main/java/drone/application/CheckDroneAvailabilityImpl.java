package drone.application;

import drone.domain.Drone;

public class CheckDroneAvailabilityImpl implements CheckDroneAvailability {

    @Override
    public boolean check(Drone drone, double packageWeight, double distanceDroneToPickup, double distancePickupToDelivery, int deliveryTimeLimit) {

        //verifica capacità di peso
        if (Drone.WEIGHT_CAPACITY < packageWeight) {
            return false;
        }

        //verifica tempo di consegna rispetto al limite dell'utente
        double deliveryTimeMinutes = ((distanceDroneToPickup + distancePickupToDelivery) / Drone.SPEED) * 60;
        if (deliveryTimeMinutes > deliveryTimeLimit) {
            return false;
        }

        //verifica disponibilità batteria
        double availableBattery = drone.getBatteryCapacity() - Drone.SAFETY_BATTERY;
        double requiredBattery = (distanceDroneToPickup + distancePickupToDelivery) * Drone.BATTERY_CONSUMPTION_PER_KM;
        if (availableBattery < requiredBattery) {
            return false;
        }

        return true;
    }
}