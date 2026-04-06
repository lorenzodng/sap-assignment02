package delivery.domain;

import buildingblocks.domain.AggregateRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//questo è un esempio della proprietà di modello indipendente del bounded context: Shipment di questo microservizio è diverso da Shipment del gestore richieste

public class Shipment implements AggregateRoot<String> {

    private static final Logger log = LoggerFactory.getLogger(Shipment.class);
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

    //costruttore per la spedizione cancellata (drone non disponibile)
    public Shipment(String id) {
        this.id = id;
        this.droneInitialPosition = null;
        this.pickupPosition = null;
        this.deliveryPosition = null;
        this.assignedAt = 0;
        this.deliverySpeed = 0;
        this.status = ShipmentStatus.CANCELLED;
    }

    //calcola la posizione attuale del drone
    public Position calculateCurrentDronePosition() {

        //se il drone non è stato assegnato
        if (droneInitialPosition == null){
            return null;
        }

        double elapsedHours = (System.currentTimeMillis() - assignedAt) / 3600000.0; //calcolo le ore trascorse dall'assegnazione del drone
        double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza percorsa dal drone

        //prima fase: drone si muove verso il luogo di ritiro
        double distanceToPickup = calculateDistance(droneInitialPosition, pickupPosition); //calcola la distanza dalla base del drone al luogo di ritiro
        if (distanceCovered < distanceToPickup) { //se la distanza percorsa è minore della distanza verso il ritiro (il drone è in viaggio)
            return interpolate(droneInitialPosition, pickupPosition, distanceCovered / distanceToPickup); //calcola la posizione
        }

        //seconda fase: drone si muove verso la destinazione
        double distanceCovered2 = distanceCovered - distanceToPickup; //aggiorno la distanza ignorando quella già percorsa verso il ritiro
        double distanceToDelivery = calculateDistance(pickupPosition, deliveryPosition); //calcola la distanza dal luogo di ritiro al luogo di destinazione
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

    //calcola la distanza in km tra due posizioni
    private double calculateDistance(Position p1, Position p2) {
        final int R = 6371;
        double dLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double dLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());
        double haversine = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(p1.getLatitude())) * Math.cos(Math.toRadians(p2.getLatitude())) * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1-haversine));
    }

    //calcola il tempo rimanente alla consegna
    public double calculateRemainingTime() {

        //se il drone non è stato assegnato
        if (droneInitialPosition == null) {
            return 0;
        }
        double elapsedHours = (System.currentTimeMillis() - assignedAt) / 3600000.0; //calcola le ore trascorse dall'assegnazione del drone
        double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza totale percorsa dal drone
        double totalDistance = calculateDistance(droneInitialPosition, pickupPosition) + calculateDistance(pickupPosition, deliveryPosition); //calcola la distanza totale che il drone deve percorrere (base->ritiro + ritiro->destinazione)
        double remainingDistance = Math.max(0, totalDistance - distanceCovered); //calcola la distanza rimanente (distanza totale - distanza già percorsa)
        return (int) Math.ceil((remainingDistance / deliverySpeed) * 60); //converte la distanza rimanente in minuti (senza secondi), arrotondando per eccesso
    }

    @Override
    public String getId() {
        return id;
    }

    //restituisce lo stato in base alla posizione del drone
    public ShipmentStatus getStatus() {
        if (droneInitialPosition != null) {
            double elapsedHours = (System.currentTimeMillis() - assignedAt) / 3600000.0; //calcola le ore trascorse dall'assegnazione del drone alla spedizione
            double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza totale percorsa dal drone
            double distanceToPickup = calculateDistance(droneInitialPosition, pickupPosition); //calcola la distanza dalla posizione iniziale del drone al luogo di ritiro
            double totalDistance = distanceToPickup + calculateDistance(pickupPosition, deliveryPosition); //calcola la distanza totale che il drone deve percorrere
            if (distanceCovered >= totalDistance) { //se il drone ha raggiunto la destinazione
                this.status = ShipmentStatus.COMPLETED;
            } else if (distanceCovered >= distanceToPickup) { //se il drone ha raggiunto il logo di ritiro
                this.status = ShipmentStatus.IN_PROGRESS;
            }
        }
        return status;
    }

}