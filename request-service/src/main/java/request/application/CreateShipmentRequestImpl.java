package request.application;

import request.domain.Package;
import request.domain.User;
import request.domain.Position;
import request.domain.Shipment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class CreateShipmentRequestImpl implements CreateShipmentRequest {

    @Override
    public Shipment create(String userId, String userName, String userSurname, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, String pickupDate, String pickupTime, Integer deliveryTimeLimit, Double weight, Boolean fragile) {
        try {
            User user = new User(userId, userName, userSurname);
            Position pickupLocation = new Position(pickupLat, pickupLon);
            Position deliveryLocation = new Position(deliveryLat, deliveryLon);
            Package pack = new Package(UUID.randomUUID().toString(), weight, fragile);

            return new Shipment(UUID.randomUUID().toString(), user, pickupLocation, deliveryLocation, LocalDate.parse(pickupDate), LocalTime.parse(pickupTime), deliveryTimeLimit, pack);
        } catch (Exception ex) {
            throw new InvalidShipmentDataException("Invalid or poorly formatted shipment data", ex);
        }
    }
}