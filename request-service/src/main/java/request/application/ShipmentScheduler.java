package request.application;

import io.vertx.core.Future;
import request.domain.Shipment;

public interface ShipmentScheduler {

    Future<Void> schedule(Shipment shipment);
}
