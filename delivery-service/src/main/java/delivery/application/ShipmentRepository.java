package delivery.application;

import buildingblocks.application.OutboundPort;
import delivery.domain.Shipment;
import java.util.Optional;

@OutboundPort
public interface ShipmentRepository {
    void save(Shipment shipment);
    Optional<Shipment> findById(String id);
}