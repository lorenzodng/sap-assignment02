package request.application;

import buildingblocks.application.InboundPort;
import request.domain.Package;
import request.domain.User;
import request.domain.Position;
import request.domain.Shipment;
import java.time.LocalDate;
import java.time.LocalTime;

@InboundPort
public interface CreateShipmentRequest {
    Shipment create(String id, User user, Position pickupLocation, Position deliveryLocation, LocalDate pickupDate, LocalTime pickupTime, int deliveryTimeLimit, Package pack);
}