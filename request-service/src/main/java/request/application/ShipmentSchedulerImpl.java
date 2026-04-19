package request.application;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import request.domain.Shipment;
import java.time.Duration;
import java.time.LocalDateTime;

public class ShipmentSchedulerImpl implements ShipmentScheduler {

    private final DroneServiceNotifier droneServiceNotifier;
    private final Vertx vertx;

    public ShipmentSchedulerImpl(DroneServiceNotifier droneServiceNotifier, Vertx vertx) {
        this.droneServiceNotifier = droneServiceNotifier;
        this.vertx = vertx;
    }

    @Override
    public Future<Void> schedule(Shipment shipment) {
        LocalDateTime pickupDateTime = LocalDateTime.of(shipment.getPickupDate(), shipment.getPickupTime());
        long delayMs = Duration.between(LocalDateTime.now(), pickupDateTime).toMillis();

        if (delayMs <= 0) {
            return droneServiceNotifier.notifyShipmentRequest(shipment);
        } else {
            vertx.setTimer(delayMs, id -> droneServiceNotifier.notifyShipmentRequest(shipment));
            return Future.succeededFuture();
        }
    }
}