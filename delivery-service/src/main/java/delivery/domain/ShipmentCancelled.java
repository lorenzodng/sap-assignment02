package delivery.domain;

public class ShipmentCancelled implements ShipmentEvent {

    private final String shipmentId;
    private final long occurredAt;

    public ShipmentCancelled(String shipmentId, long occurredAt) {
        this.shipmentId = shipmentId;
        this.occurredAt = occurredAt;
    }

    @Override
    public String getShipmentId() {
        return shipmentId;
    }

    @Override
    public long getOccurredAt() {
        return occurredAt;
    }
}