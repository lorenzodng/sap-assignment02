package drone.application;

import drone.domain.Drone;
import drone.domain.GeoUtils;
import drone.domain.Position;
import java.util.List;

public class AssignDroneImpl implements AssignDrone {

    private final CheckDroneAvailability checkDroneAvailability;

    public AssignDroneImpl(CheckDroneAvailability checkDroneAvailability) {
        this.checkDroneAvailability = checkDroneAvailability;
    }

    //assegna il drone alla spedizione
    @Override
    public Drone assign(List<Drone> drones, double packageWeight, double pickupLatitude, double pickupLongitude, double distancePickupToDelivery, int deliveryTimeLimit) {
        Position pickupPosition = new Position(pickupLatitude, pickupLongitude);

        /*
        converte la lista in uno stream, poi:
         1) filtra i droni disponibili
         2) per ogni drone disponibile:
         - calcola distanza drone -> ritiro
         - verifica le caratteristiche del drone
         3) tra tutti quelli che hanno passato i filtri, trova quello che ha distanza minima dal luogo di ritiro (drone 1 vs drone 2, drone 2 vs drone 3, ecc.), altrimenti restituisce null
         */
        return drones.stream().filter(Drone::isAvailable).filter(drone -> {
            double distanceDroneToPickup = calculateDistanceInKm(drone.getPosition(), pickupPosition);
            return checkDroneAvailability.check(drone, packageWeight, distanceDroneToPickup, distancePickupToDelivery, deliveryTimeLimit);
        }).min((d1, d2) -> Double.compare(calculateDistanceInKm(d1.getPosition(), pickupPosition), calculateDistanceInKm(d2.getPosition(), pickupPosition))).orElse(null);
    }

    //calcola la distanza in km tra la base e il luogo di pickup
    private double calculateDistanceInKm(Position p1, Position p2) {
        return GeoUtils.haversine(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }
}