package drone.application;

import drone.domain.Drone;
import drone.domain.GeoUtils;
import java.util.List;

public class AssignDroneImpl implements AssignDrone {

    private final CheckDroneAvailability checkDroneAvailability;

    public AssignDroneImpl(CheckDroneAvailability checkDroneAvailability) {
        this.checkDroneAvailability = checkDroneAvailability;
    }

    //assegna il drone alla spedizione
    @Override
    public Drone assign(List<Drone> drones, double packageWeight, double pickupLatitude, double pickupLongitude, double distancePickupToDelivery, int deliveryTimeLimit) {
        /*
        converte la lista in uno stream, poi:
         1) filtra i droni disponibili
         2) per ogni drone disponibile:
         - calcola distanza drone -> ritiro
         - verifica le caratteristiche del drone
         3) tra tutti quelli che hanno passato i filtri, trova quello che ha distanza minima dal luogo di ritiro (drone 1 vs drone 2, drone 2 vs drone 3, ecc.), altrimenti restituisce null
         */
        return drones.stream().filter(drone -> drone.isAvailable()).filter(drone -> {
            double distanceDroneToPickup = GeoUtils.haversine(drone.getPosition().getLatitude(), drone.getPosition().getLongitude(), pickupLatitude, pickupLongitude);
            return checkDroneAvailability.check(drone, packageWeight, distanceDroneToPickup, distancePickupToDelivery, deliveryTimeLimit);
        }).min((d1, d2) -> {
            double dist1 = GeoUtils.haversine(d1.getPosition().getLatitude(), d1.getPosition().getLongitude(), pickupLatitude, pickupLongitude);
            double dist2 = GeoUtils.haversine(d2.getPosition().getLatitude(), d2.getPosition().getLongitude(), pickupLatitude, pickupLongitude);
            return Double.compare(dist1, dist2);
        }).orElse(null);
    }
}