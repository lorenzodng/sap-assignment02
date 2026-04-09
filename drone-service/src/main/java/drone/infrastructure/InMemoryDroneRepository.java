package drone.infrastructure;

import drone.application.DroneRepository;
import drone.domain.Drone;
import java.util.List;

//gestisce lo stato dei droni in memoria
public class InMemoryDroneRepository implements DroneRepository {
    private final List<Drone> drones; //lista di tutti i droni esistenti

    public InMemoryDroneRepository(List<Drone> drones) {
        this.drones = drones;
    }

    //recupera tutti i droni esistenti
    @Override
    public List<Drone> findAll() {
        return drones;
    }

    //aggiorna il drone come non disponibile
    @Override
    public void updateAvailability(String droneId, boolean available) {
        drones.stream().filter(d -> d.getId().equals(droneId)).findFirst().ifPresent(d -> d.setAvailable(available));
    }
}