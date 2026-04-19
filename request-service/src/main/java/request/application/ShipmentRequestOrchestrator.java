package request.application;

import buildingblocks.application.InboundPort;
import io.vertx.core.Future;
import request.domain.Shipment;

@InboundPort
public interface ShipmentRequestOrchestrator {
    Future<Shipment> orchestrateRequest(String userId, String userName, String userSurname, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, String pickupDate, String pickupTime, Integer deliveryTimeLimit, Double weight, Boolean fragile);
}