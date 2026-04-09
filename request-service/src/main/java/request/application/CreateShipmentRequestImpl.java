package request.application;

import request.domain.Package;
import request.domain.User;
import request.domain.Position;
import request.domain.Shipment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

//contiene oggetti factory dedicati solo alla creazione della richiesta di spedizione
public class CreateShipmentRequestImpl implements CreateShipmentRequest {

    /*
    crea la spedizione
    sono passati i valori "di base" perchè, essendo il metodo richiamato da ShipmentRequestController al livello infrastructure, non dovrebbe creare elementi di dominio (User, Position, Package)
    */
    @Override
    public Shipment create(String userId, String userName, String userSurname, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, String pickupDate, String pickupTime, Integer deliveryTimeLimit, Double weight, Boolean fragile) {

        User user = new User(userId, userName, userSurname);
        Position pickupLocation = new Position(pickupLat, pickupLon);
        Position deliveryLocation = new Position(deliveryLat, deliveryLon);
        Package pack = new Package(UUID.randomUUID().toString(), weight, fragile);

        Shipment shipment = new Shipment(UUID.randomUUID().toString(), user, pickupLocation, deliveryLocation, LocalDate.parse(pickupDate), LocalTime.parse(pickupTime), deliveryTimeLimit, pack);
        return shipment;
    }
}