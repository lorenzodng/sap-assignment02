package request.application;

import request.domain.Position;
import request.domain.Shipment;
import java.time.LocalDateTime;

//verifica che i dati della richiesti siano validi (non che la consegna sia fattibile)
public class ValidateShipmentRequestImpl implements ValidateShipmentRequest {

    @Override
    public void validate(Shipment shipment) {

        LocalDateTime pickupDateTime = LocalDateTime.of(shipment.getPickupDate(), shipment.getPickupTime());

        // verifica che la data e l'ora di ritiro siano nel futuro
        if (pickupDateTime.isBefore(LocalDateTime.now())) {
            throw new ShipmentValidationException("Pickup date and time must be in the future");
        }

        // verifica che il peso del pacco sia maggiore di zero
        if (shipment.getPackage().getWeight() <= 0) {
            throw new ShipmentValidationException("Package weight must be greater than zero");
        }

        // verifica che il limite di tempo sia maggiore di zero
        if (shipment.getDeliveryTimeLimit() <= 0) {
            throw new ShipmentValidationException("Delivery time limit must be positive");
        }

        //controlla coordinate (vedi nota sotto)
        checkCoordinates(shipment.getPickupLocation(), "Pickup");
        checkCoordinates(shipment.getDeliveryLocation(), "Delivery");
    }

    private void checkCoordinates(Position pos, String label) {
        if (pos.getLatitude() < -90 || pos.getLatitude() > 90 || pos.getLongitude() < -180 || pos.getLongitude() > 180) {
            throw new ShipmentValidationException(label + " coordinates are out of range");
        }
    }
}