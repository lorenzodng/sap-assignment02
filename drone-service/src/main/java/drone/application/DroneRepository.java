package drone.application;

import buildingblocks.application.OutboundPort;
import drone.domain.Drone;
import java.util.List;

@OutboundPort
public interface DroneRepository {
    List<Drone> findAll();

    void updateAvailability(String droneId, boolean available);
}