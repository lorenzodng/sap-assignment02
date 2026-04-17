package request.application;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import request.domain.*;
import request.domain.Package;
import request.infrastructure.DroneServiceClient;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.assertTrue;

//integration test
@ExtendWith(VertxExtension.class)
class ShipmentSchedulerIntegrationTest {

    //verifica che una richiesta valida notifichi il drone-service
    @Test
    void immediatePickupNotifiesDroneService(VertxTestContext testContext) {
        Vertx vertx = Vertx.vertx();
        AtomicBoolean requestReceived = new AtomicBoolean(false);

        vertx.createHttpServer().requestHandler(req -> {
            requestReceived.set(true);
            req.response().setStatusCode(200).end();
        }).listen(8081).onSuccess(server -> {
            String id = UUID.randomUUID().toString();
            User user = new User("user-1", "Mario", "Rossi");
            Position pickupLocation = new Position(41.90, 12.49);
            Position deliveryLocation = new Position(41.92, 12.51);
            LocalDate date = LocalDate.of(2020, 1, 1);
            LocalTime time = LocalTime.of(10, 0);
            int timeLimit = 60;
            Package shipmentPackage = new Package(UUID.randomUUID().toString(), 1.5, false);
            Shipment shipment = new Shipment(id, user, pickupLocation, deliveryLocation, date, time, timeLimit, shipmentPackage);

            DroneServiceNotifier notifier = new DroneServiceClient(vertx, "http://localhost:8081");
            ShipmentSchedulerImpl scheduler = new ShipmentSchedulerImpl(notifier, vertx);
            scheduler.schedule(shipment)
                    .onSuccess(v -> {
                        assertTrue(requestReceived.get());
                        testContext.completeNow();
                    })
                    .onFailure(testContext::failNow);
        });
    }
}