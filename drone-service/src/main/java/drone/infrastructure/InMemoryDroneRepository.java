package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DroneRepository;
import drone.domain.Drone;
import java.util.List;

@Adapter
public class InMemoryDroneRepository implements DroneRepository {

    private final List<Drone> drones;

    public InMemoryDroneRepository(List<Drone> drones) {
        this.drones = drones;
    }

    @Override
    public List<Drone> findAll() {
        return drones;
    }

    @Override
    public void updateAvailability(String droneId, boolean available) {
        Drone drone = drones.stream().filter(d -> d.getId().equals(droneId)).findFirst().orElseThrow(DroneNotFoundException::new);
        drone.setAvailable(available);
    }
}