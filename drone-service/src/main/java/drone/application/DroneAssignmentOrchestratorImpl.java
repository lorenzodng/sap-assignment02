package drone.application;

import drone.domain.Drone;
import drone.domain.GeoUtils;
import io.vertx.core.Future;
import java.util.List;

//orchestratore che coordina il flusso principale di assegnazione di un drone
public class DroneAssignmentOrchestratorImpl implements DroneAssignmentOrchestrator {

    private final AssignDrone assignDrone;
    private final DeliveryServiceNotifier notifier;
    private final DroneRepository repository;
    private final DroneMetrics metrics; // La dipendenza

    public DroneAssignmentOrchestratorImpl(AssignDrone assignDrone, DeliveryServiceNotifier notifier, DroneRepository repository, DroneMetrics metrics) {
        this.assignDrone = assignDrone;
        this.notifier = notifier;
        this.repository = repository;
        this.metrics = metrics;
    }

    //gestisce l'assegnazione del drone alla spedizione
    @Override
    public Future<Void> orchestrateAssignment(String shipmentId, double pickupLat, double pickupLon, double deliveryLat, double deliveryLon, double weight, int timeLimit) {

        //step 1: assegna il drone
        double distance = GeoUtils.haversine(pickupLat, pickupLon, deliveryLat, deliveryLon);
        List<Drone> drones = repository.findAll(); //recupera tutti i droni esistenti
        Drone assignedDrone = assignDrone.assign(drones, weight, pickupLat, pickupLon, distance, timeLimit); //assegna il drone
        if (assignedDrone != null) { //se esiste un drone
            metrics.incrementAssignment(true);
            repository.updateAvailability(assignedDrone.getId(), false); //imposta il drone come non più disponibile

            //step 2: pubblica l'evento
            return notifier.notifyDroneAssigned(shipmentId, assignedDrone, pickupLat, pickupLon, deliveryLat, deliveryLon);
        } else { //se non esiste un drone
            metrics.incrementAssignment(false);
            return notifier.notifyDroneNotAvailable(shipmentId).compose(v -> Future.failedFuture("NO_DRONE_AVAILABLE"));
        }
    }
}
