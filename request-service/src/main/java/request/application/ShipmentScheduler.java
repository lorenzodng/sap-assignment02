package request.application;

import buildingblocks.application.OutboundPort;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import request.domain.Shipment;

@OutboundPort
public interface ShipmentScheduler {

    Future<Void> schedule(Shipment shipment, DroneServiceNotifier notifier, Vertx vertx);
}
