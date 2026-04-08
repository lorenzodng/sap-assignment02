package request.application;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import request.domain.Shipment;
import java.time.LocalDateTime;

//verifica se è il tempo di assegnare il drone
public class ShipmentSchedulerImpl implements ShipmentScheduler {

    @Override
    public Future<Void> schedule(Shipment shipment, DroneServiceNotifier droneServiceNotifier, Vertx vertx) {
        LocalDateTime pickupDateTime = LocalDateTime.of(shipment.getPickupDate(), shipment.getPickupTime());
        long delayMs = java.time.Duration.between(LocalDateTime.now(), pickupDateTime).toMillis();

        //se la data/ora è già passata o è adesso, il drone parte
        if (delayMs <= 0) {
            return droneServiceNotifier.notifyShipmentRequest(shipment);
            //altrimenti attende
        } else {
            vertx.setTimer(delayMs, id -> droneServiceNotifier.notifyShipmentRequest(shipment));
            return Future.succeededFuture();
        }
    }
}