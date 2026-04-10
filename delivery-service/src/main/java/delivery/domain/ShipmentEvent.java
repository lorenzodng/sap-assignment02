package delivery.domain;

public interface ShipmentEvent {
    String getShipmentId();

    long getOccurredAt();
}