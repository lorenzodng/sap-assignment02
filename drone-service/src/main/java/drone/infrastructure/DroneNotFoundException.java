package drone.infrastructure;

public class DroneNotFoundException extends RuntimeException {
    public String getMessage() {
        return "Drone not found in repository";
    }
}