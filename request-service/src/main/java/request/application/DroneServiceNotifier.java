package request.application;

import buildingblocks.application.OutboundPort;
import io.vertx.core.Future;
import request.domain.Shipment;

@OutboundPort
public interface DroneServiceNotifier {
    Future<Void> notifyShipmentRequest(Shipment shipment);
}
