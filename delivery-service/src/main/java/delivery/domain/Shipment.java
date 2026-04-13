package delivery.domain;

import buildingblocks.domain.AggregateRoot;
import java.util.List;

//questo è un esempio della proprietà di modello indipendente del bounded context: Shipment di questo microservizio è diverso da Shipment del gestore richieste

public class Shipment implements AggregateRoot<String> {

    private static final double MS_TO_HOURS = 3600000.0;
    private static final int MINUTES_IN_HOUR = 60;
    private final String id;
    private Position droneInitialPosition;
    private Position pickupPosition;
    private Position deliveryPosition;
    private long assignedAt;
    private double deliverySpeed;
    private ShipmentStatus status;

    //costruttore principale
    public Shipment(String id, Position droneInitialPosition, Position pickupPosition, Position deliveryPosition, long assignedAt, double deliverySpeed) {
        this.id = id;
        this.droneInitialPosition = droneInitialPosition;
        this.pickupPosition = pickupPosition;
        this.deliveryPosition = deliveryPosition;
        this.assignedAt = assignedAt;
        this.deliverySpeed = deliverySpeed;
        this.status = ShipmentStatus.SCHEDULED;
    }

    //calcola la posizione attuale del drone
    public Position calculateCurrentDronePosition() {

        //se il drone non è stato assegnato
        if (droneInitialPosition == null) {
            return null;
        }

        double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS; //calcolo le ore trascorse dall'assegnazione del drone
        double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza percorsa dal drone

        //prima fase: drone si muove verso il luogo di ritiro
        double distanceToPickup = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude()); //calcola la distanza dalla base del drone al luogo di ritiro
        if (distanceCovered < distanceToPickup) { //se la distanza percorsa è minore della distanza verso il ritiro (il drone è in viaggio)
            return interpolate(droneInitialPosition, pickupPosition, distanceCovered / distanceToPickup); //calcola la posizione
        }

        //seconda fase: drone si muove verso la destinazione
        double distanceCovered2 = distanceCovered - distanceToPickup; //aggiorno la distanza ignorando quella già percorsa verso il ritiro
        double distanceToDelivery = GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude()); //calcola la distanza dal luogo di ritiro al luogo di destinazione
        if (distanceCovered2 < distanceToDelivery) { //se la distanza percorsa è minore della distanza verso la destinazione (il drone è in viaggio)
            return interpolate(pickupPosition, deliveryPosition, distanceCovered2 / distanceToDelivery); //calcola la posizione
        }

        // drone arrivato a destinazione
        return deliveryPosition;
    }

    //calcola la posizione intermedia tra due punti
    private Position interpolate(Position from, Position to, double fraction) {
        double lat = from.getLatitude() + (to.getLatitude() - from.getLatitude()) * fraction;
        double lon = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * fraction;
        return new Position(lat, lon);
    }

    //calcola il tempo rimanente alla consegna
    public double calculateRemainingTime() {

        //se il drone non è stato assegnato
        if (droneInitialPosition == null) {
            return 0;
        }
        double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS; //calcola le ore trascorse dall'assegnazione del drone
        double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza totale percorsa dal drone
        double totalDistance = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude()) + GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude()); //calcola la distanza totale che il drone deve percorrere (base->ritiro + ritiro->destinazione)
        double remainingDistance = Math.max(0, totalDistance - distanceCovered); //calcola la distanza rimanente (distanza totale - distanza già percorsa)
        return (int) Math.ceil((remainingDistance / deliverySpeed) * MINUTES_IN_HOUR); //converte la distanza rimanente in minuti (senza secondi), arrotondando per eccesso
    }

    @Override
    public String getId() {
        return id;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    //restituisce lo stato in base alla posizione del drone
    public ShipmentStatus updateStatus() {
        if (droneInitialPosition != null) {
            double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS; //calcola le ore trascorse dall'assegnazione del drone alla spedizione
            double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza totale percorsa dal drone
            double distanceToPickup = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude()); //calcola la distanza dalla posizione iniziale del drone al luogo di ritiro
            double totalDistance = distanceToPickup + GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude()); //calcola la distanza totale che il drone deve percorrere
            if (distanceCovered >= totalDistance) { //se il drone ha raggiunto la destinazione
                this.status = ShipmentStatus.COMPLETED;
            } else if (distanceCovered >= distanceToPickup) { //se il drone ha raggiunto il logo di ritiro
                this.status = ShipmentStatus.IN_PROGRESS;
            }
        }
        return status;
    }

    //per event sourcing

    //ricostruisce lo stato della spedizione
    public static Shipment reconstitute(List<ShipmentEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Cannot reconstitute shipment: no events provided");
        }
        Shipment shipment = null;
        for (ShipmentEvent event : events) {
            if (event instanceof ShipmentAssigned e) { //se trova un ShipmentAssigned
                shipment = new Shipment(e.getShipmentId(), e.getDroneInitialPosition(), e.getPickupPosition(), e.getDeliveryPosition(), e.getOccurredAt(), e.getDeliverySpeed()); //crea una spedizione
            } else if (event instanceof ShipmentCompleted) { //se trova un ShipmentCompleted
                if (shipment != null) {
                    shipment.status = ShipmentStatus.COMPLETED; //aggiorna lo stato a COMPLETED
                }
            } else if (event instanceof ShipmentCancelled) { //se trova un ShipmentCancelled
                if (shipment != null) {
                    shipment.status = ShipmentStatus.CANCELLED; //aggiorna lo stato a CANCELLED
                }
            }
        }

        if (shipment == null) {
            throw new IllegalStateException("Reconstitution failed: ShipmentAssigned event missing");
        }

        return shipment;
    }
}