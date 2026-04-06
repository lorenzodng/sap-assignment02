package gateway.application;

import buildingblocks.application.OutboundPort;

@OutboundPort
public interface ApiGatewayObserver {
    void notifyShipmentRequest();
    void notifyTrackingRequest();
    void notifyValidShipmentRequest();
    void notifyDroneAvailable();
    void notifyDroneNotAvailable();
}