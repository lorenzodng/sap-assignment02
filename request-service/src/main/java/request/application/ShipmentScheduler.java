package request.application;

import buildingblocks.application.OutboundPort;
import io.vertx.core.Future;
import request.domain.Shipment;

@OutboundPort
public interface ShipmentScheduler {

    Future<Void> schedule(Shipment shipment);
}
