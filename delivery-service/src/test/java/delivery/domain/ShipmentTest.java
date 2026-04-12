package delivery.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//unit test
class ShipmentTest {

    //verifica la validità della posizione del drone
    @Test
    void droneMovesTowardsPickup() {
        String id = "test-id";
        Position droneInitial = new Position(41.90, 12.49);
        Position pickup = new Position(41.91, 12.50);
        Position delivery = new Position(41.92, 12.51);
        long thirtySecondsInMillis = 30 * 1000;
        long assignedAt = System.currentTimeMillis() - thirtySecondsInMillis;
        double speed = 100.0;

        Shipment shipment = new Shipment(id, droneInitial, pickup, delivery, assignedAt, speed);
        Position result = shipment.calculateCurrentDronePosition();

        assertNotNull(result);
        assertTrue(result.getLatitude() > droneInitial.getLatitude());
        assertTrue(result.getLatitude() < pickup.getLatitude());
    }
}