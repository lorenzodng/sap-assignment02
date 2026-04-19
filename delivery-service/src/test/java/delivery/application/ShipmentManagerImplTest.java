package delivery.application;

import delivery.domain.*;
import delivery.infrastructure.InMemoryShipmentEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//sociable unit test
class ShipmentManagerImplTest {

    private ShipmentManagerImpl manager;

    @BeforeEach
    void setUp() {
        ShipmentEventStore eventStore = new InMemoryShipmentEventStore();
        manager = new ShipmentManagerImpl(eventStore, null);
    }

    @Test
    void shipmentIsScheduledAfterAssignment() {
        String id = "test-id";
        boolean assigned = true;
        double droneLat = 41.90;
        double droneLon = 12.49;
        double pickupLat = 41.91;
        double pickupLon = 12.50;
        double deliveryLat = 41.92;
        double deliveryLon = 12.51;
        long assignedAt = System.currentTimeMillis();
        double speed = 100.0;
        manager.createShipmentFromAssignment(id, assigned, droneLat, droneLon, pickupLat, pickupLon, deliveryLat, deliveryLon, assignedAt, speed);
        Shipment shipment = manager.getShipmentDetails(id);
        assertNotNull(shipment);
        assertEquals(ShipmentStatus.SCHEDULED, shipment.getStatus());
    }
}