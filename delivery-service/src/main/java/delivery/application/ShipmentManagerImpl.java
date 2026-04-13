package delivery.application;

import delivery.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

//punto centrale di gestione di spedizione
public class ShipmentManagerImpl implements ShipmentManager {

    private static final Logger log = LoggerFactory.getLogger(ShipmentManagerImpl.class);
    private final ShipmentEventStore eventStore;
    private final DeliveryMetrics metrics; // Nuova dipendenza

    public ShipmentManagerImpl(ShipmentEventStore eventStore, DeliveryMetrics metrics) {
        this.eventStore = eventStore;
        this.metrics = metrics;
    }

    /*
    crea la spedizione scheduled o cancelled
    sono passati i valori "di base" perchè, essendo il metodo richiamato da ShipmentAssignment al livello infrastructure, non dovrebbe creare elementi di dominio (e quindi Position)
    */
    @Override
    public void createShipmentFromAssignment(String id, boolean assigned, Double droneLat, Double droneLon, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, Long assignedAt, Double speed) {
        if (assigned) {
            metrics.incrementActive();
            ShipmentEvent event = new ShipmentAssigned(id, new Position(droneLat, droneLon), new Position(pickupLat, pickupLon), new Position(deliveryLat, deliveryLon), speed, assignedAt); //crea l'evento di assegnazione drone
            eventStore.append(event); //lo aggiunge all'event store
            log.info("Shipment {} scheduled", id);
        } else {
            ShipmentEvent event = new ShipmentCancelled(id, System.currentTimeMillis()); //crea l'evento di consegna cancellata
            eventStore.append(event); //lo aggiunge all'event store
            log.info("Shipment {} cancelled", id);
        }
    }

    //recupera gli eventi e ricostruisce l'aggregato per recuperare le informazioni della spedizione
    @Override
    public Shipment getShipmentDetails(String id) {
        List<ShipmentEvent> events = eventStore.findByShipmentId(id); //recupera gli eventi della spedizione
        if (events.isEmpty()) {
            throw new ShipmentNotFoundException();
        }
        return Shipment.reconstitute(events); //ricostruisce l'aggregato
    }

    //controlla se la consegna è stata completata
    @Override
    public void checkAndCompleteShipment(String id) {
        List<ShipmentEvent> events = eventStore.findByShipmentId(id);
        if (events.isEmpty()) {
            throw new ShipmentNotFoundException();
        }
        Shipment shipment = Shipment.reconstitute(events);
        boolean alreadyCompleted = events.stream().anyMatch(e -> e instanceof ShipmentCompleted); //controlla se esiste già un evento di consegna completata
        if (!alreadyCompleted && shipment.updateStatus() == ShipmentStatus.COMPLETED) { //se non esiste un evento di consegna completata e lo stato è COMPLETED
            metrics.incrementCompleted();
            ShipmentEvent event = new ShipmentCompleted(id, System.currentTimeMillis()); //crea l'evento di consegna completata
            eventStore.append(event); //lo aggiunge all'event store
            log.info("Shipment {} completed", id);
        }
    }
}