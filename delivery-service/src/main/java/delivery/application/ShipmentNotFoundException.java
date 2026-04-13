package delivery.application;

public class ShipmentNotFoundException extends RuntimeException {
    public String getMessage() {
        return "Shipment not found in repository";
    }
}