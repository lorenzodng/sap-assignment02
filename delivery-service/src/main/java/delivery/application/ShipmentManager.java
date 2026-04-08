package delivery.application;

import buildingblocks.application.InboundPort;
import delivery.domain.Shipment;

@InboundPort
public interface ShipmentManager {
    void createShipmentFromAssignment(String id, boolean assigned, Double droneLat, Double droneLon, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, Long assignedAt, Double speed);
    Shipment getShipmentDetails(String id);
}