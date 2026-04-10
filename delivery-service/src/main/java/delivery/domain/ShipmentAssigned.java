package delivery.domain;

//evento di assegnazione drone - contiene tutte le informazioni necessarie per ricostruire lo stato dell'aggregato spedizione
public class ShipmentAssigned implements ShipmentEvent {

    private final String shipmentId;
    private final Position droneInitialPosition;
    private final Position pickupPosition;
    private final Position deliveryPosition;
    private final double deliverySpeed;
    private final long occurredAt;

    public ShipmentAssigned(String shipmentId, Position droneInitialPosition, Position pickupPosition, Position deliveryPosition, double deliverySpeed, long occurredAt) {
        this.shipmentId = shipmentId;
        this.droneInitialPosition = droneInitialPosition;
        this.pickupPosition = pickupPosition;
        this.deliveryPosition = deliveryPosition;
        this.deliverySpeed = deliverySpeed;
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

    public Position getDroneInitialPosition() {
        return droneInitialPosition;
    }

    public Position getPickupPosition() {
        return pickupPosition;
    }

    public Position getDeliveryPosition() {
        return deliveryPosition;
    }

    public double getDeliverySpeed() {
        return deliverySpeed;
    }
}