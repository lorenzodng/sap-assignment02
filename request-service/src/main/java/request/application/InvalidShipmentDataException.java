package request.application;

public class InvalidShipmentDataException extends RuntimeException {
    public InvalidShipmentDataException(String s, Throwable p1) {
        super(s, p1);
    }
}
