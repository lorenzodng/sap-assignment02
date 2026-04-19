package delivery.application;

import delivery.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class ShipmentManagerImpl implements ShipmentManager {

    private static final Logger log = LoggerFactory.getLogger(ShipmentManagerImpl.class);
    private final ShipmentEventStore eventStore;
    private final DeliveryMetrics metrics;

    public ShipmentManagerImpl(ShipmentEventStore eventStore, DeliveryMetrics metrics) {
        this.eventStore = eventStore;
        this.metrics = metrics;
    }

    @Override
    public void createShipmentFromAssignment(String id, boolean assigned, Double droneLat, Double droneLon, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, Long assignedAt, Double speed) {
        if (assigned) {
            metrics.incrementActive();
            ShipmentEvent event = new ShipmentAssigned(id, new Position(droneLat, droneLon), new Position(pickupLat, pickupLon), new Position(deliveryLat, deliveryLon), speed, assignedAt);
            eventStore.append(event);
            log.info("Shipment {} scheduled", id);
        } else {
            ShipmentEvent event = new ShipmentCancelled(id, System.currentTimeMillis());
            eventStore.append(event);
            log.info("Shipment {} cancelled", id);
        }
    }

    @Override
    public Shipment getShipmentDetails(String id) {
        List<ShipmentEvent> events = eventStore.findByShipmentId(id);
        if (events.isEmpty()) {
            throw new ShipmentNotFoundException();
        }
        return Shipment.reconstitute(events);
    }

    @Override
    public void checkAndCompleteShipment(String id) {
        List<ShipmentEvent> events = eventStore.findByShipmentId(id);
        if (events.isEmpty()) {
            throw new ShipmentNotFoundException();
        }
        Shipment shipment = Shipment.reconstitute(events);
        boolean alreadyCompleted = events.stream().anyMatch(e -> e instanceof ShipmentCompleted);
        if (!alreadyCompleted && shipment.updateStatus() == ShipmentStatus.COMPLETED) {
            metrics.incrementCompleted();
            ShipmentEvent event = new ShipmentCompleted(id, System.currentTimeMillis());
            eventStore.append(event);
            log.info("Shipment {} completed", id);
        }
    }
}