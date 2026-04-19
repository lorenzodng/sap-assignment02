package request.application;

import request.domain.Position;
import request.domain.Shipment;
import java.time.LocalDateTime;

public class ValidateShipmentRequestImpl implements ValidateShipmentRequest {

    @Override
    public void validate(Shipment shipment) {

        LocalDateTime pickupDateTime = LocalDateTime.of(shipment.getPickupDate(), shipment.getPickupTime());

        if (pickupDateTime.isBefore(LocalDateTime.now())) {
            throw new ShipmentValidationException("Pickup date and time must be in the future");
        }

        if (shipment.getPackage().getWeight() <= 0) {
            throw new ShipmentValidationException("Package weight must be greater than zero");
        }

        if (shipment.getDeliveryTimeLimit() <= 0) {
            throw new ShipmentValidationException("Delivery time limit must be positive");
        }

        checkCoordinates(shipment.getPickupLocation(), "Pickup");
        checkCoordinates(shipment.getDeliveryLocation(), "Delivery");
    }

    private void checkCoordinates(Position pos, String label) {
        if (pos.getLatitude() < -90 || pos.getLatitude() > 90 || pos.getLongitude() < -180 || pos.getLongitude() > 180) {
            throw new ShipmentValidationException(label + " coordinates are out of range");
        }
    }
}