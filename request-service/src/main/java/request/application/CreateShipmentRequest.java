package request.application;

import request.domain.Shipment;

public interface CreateShipmentRequest {
    Shipment create(String userId, String userName, String userSurname, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, String pickupDate, String pickupTime, Integer timeLimit, Double weight, Boolean fragile);
}