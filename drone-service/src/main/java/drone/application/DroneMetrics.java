package drone.application;

import buildingblocks.application.OutboundPort;

@OutboundPort
public interface DroneMetrics {
    void incrementAssignment(boolean success);
}
