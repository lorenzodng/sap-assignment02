package request.application;

import io.vertx.core.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.domain.*;
import request.domain.Package;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import static org.mockito.Mockito.*;

//integration test
class ShipmentSchedulerImplTest {

    private DroneServiceNotifier droneServiceNotifier;
    private ShipmentSchedulerImpl scheduler;

    //crea mock e istanze necessarie
    @BeforeEach
    void setUp() {
        droneServiceNotifier = mock(DroneServiceNotifier.class);
        scheduler = new ShipmentSchedulerImpl(droneServiceNotifier, null);
    }

    //verifica che una richiesta valida notifichi il drone-service
    @Test
    void immediatePickupNotifiesDroneService() {
        when(droneServiceNotifier.notifyShipmentRequest(any())).thenReturn(Future.succeededFuture()); //quando "notifyShipmentRequest" viene chiamato, restituisce la future di successo
        String id = UUID.randomUUID().toString();
        User user = new User("user-1", "Mario", "Rossi");
        Position pickupLocation = new Position(41.90, 12.49);
        Position deliveryLocation = new Position(41.92, 12.51);
        LocalDate date = LocalDate.of(2030, 1, 1);
        LocalTime time = LocalTime.of(10, 0);
        int timeLimit = 60;
        Package shipmentPackage = new Package(UUID.randomUUID().toString(), 1.5, false);
        Shipment shipment = new Shipment(id, user, pickupLocation, deliveryLocation, date, time, timeLimit, shipmentPackage);
        scheduler.schedule(shipment);

        verify(droneServiceNotifier, times(1)).notifyShipmentRequest(any()); //verifica che "notifyShipmentRequest" sia stato chiamato esattamente una volta
    }
}