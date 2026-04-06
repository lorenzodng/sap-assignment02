package drone.application;

import drone.domain.Drone;
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
        return drones.stream().filter(drone -> drone.isAvailable()).filter(drone -> {
            double distanceDroneToPickup = calculateDistance(drone.getPosition(), pickupPosition);
            return checkDroneAvailability.check(drone, packageWeight, distanceDroneToPickup, distancePickupToDelivery, deliveryTimeLimit);
        }).min((d1, d2) -> Double.compare(calculateDistance(d1.getPosition(), pickupPosition), calculateDistance(d2.getPosition(), pickupPosition))).orElse(null);
    }

    //calcola la distanza in km tra due posizioni
    private double calculateDistance(Position p1, Position p2) {
        final int R = 6371; //raggio della Terra in km
        double dLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double dLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());
        double haversine = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(p1.getLatitude())) * Math.cos(Math.toRadians(p2.getLatitude())) * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1-haversine));
    }
}