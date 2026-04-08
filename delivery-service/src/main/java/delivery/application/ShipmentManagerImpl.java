package delivery.application;

import delivery.domain.Position;
import delivery.domain.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//punto centrale di gestione di spedizione
public class ShipmentManagerImpl implements ShipmentManager {

    private static final Logger log = LoggerFactory.getLogger(ShipmentManagerImpl.class);
    private final ShipmentRepository repository;
    private final DeliveryMetrics metrics; // Nuova dipendenza

    public ShipmentManagerImpl(ShipmentRepository repository, DeliveryMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    /*
    crea la spedizione scheduled o cancelled
    sono passati i valori "di base" perchè, essendo il metodo richiamato da ShipmentAssignment al livello infrastructure, non dovrebbe creare elementi di dominio (e quindi Position)
    */
    @Override
    public void createShipmentFromAssignment(String id, boolean assigned, Double droneLat, Double droneLon, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, Long assignedAt, Double speed) {
        Shipment shipment;
        if (assigned) {
            metrics.incrementActive();
            shipment = new Shipment(id, new Position(droneLat, droneLon), new Position(pickupLat, pickupLon), new Position(deliveryLat, deliveryLon), assignedAt, speed);
            log.info("Shipment {} scheduled", id);

        } else {
            shipment = new Shipment(id);
            log.info("Shipment {} cancelled", id);
        }
        repository.save(shipment);
    }

    //recupera le informazioni di una spedizione
    @Override
    public Shipment getShipmentDetails(String id) {
        return repository.findById(id).map(shipment -> {
            if (shipment.isJustCompleted()) { //se la consegna è completata
                metrics.incrementCompleted(); //aggiorna la metrica
                shipment.markCompletionAsNotified(); //aggiorna la flag in modo che la metrica venga incrementata solo una volta (e non ogni volta che si chiama getStatus in Shipment)
                repository.save(shipment); //aggiorna la spedizione nella mappa
            }
            return shipment;
        }).orElse(null);
    }
}