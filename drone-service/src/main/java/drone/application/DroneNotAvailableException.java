package drone.application;

public class DroneNotAvailableException extends RuntimeException {
    public String getMessage() {
        return "No suitable drone found for this shipment";
    }
}