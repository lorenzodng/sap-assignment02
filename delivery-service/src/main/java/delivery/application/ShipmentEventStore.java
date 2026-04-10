package delivery.application;

import buildingblocks.application.OutboundPort;
import delivery.domain.ShipmentEvent;
import java.util.List;

@OutboundPort
public interface ShipmentEventStore {
    void append(ShipmentEvent event);

    List<ShipmentEvent> findByShipmentId(String shipmentId);
}