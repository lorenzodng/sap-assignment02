package delivery.domain;

//evento di completamento consegna - contiene tutte le informazioni necessarie per ricostruire lo stato dell'aggregato spedizione
public class ShipmentCompleted implements ShipmentEvent {

    private final String shipmentId;
    private final long occurredAt;

    public ShipmentCompleted(String shipmentId, long occurredAt) {
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