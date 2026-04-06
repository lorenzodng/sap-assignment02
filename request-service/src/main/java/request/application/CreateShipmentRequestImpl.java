package request.application;

import request.domain.Package;
import request.domain.User;
import request.domain.Position;
import request.domain.Shipment;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateShipmentRequestImpl implements CreateShipmentRequest {

    @Override
    public Shipment create(String id, User user, Position pickupLocation, Position deliveryLocation, LocalDate pickupDate, LocalTime pickupTime, int deliveryTimeLimit, Package pack) {
        return new Shipment(id, user, pickupLocation, deliveryLocation, pickupDate, pickupTime, deliveryTimeLimit, pack);
    }
}