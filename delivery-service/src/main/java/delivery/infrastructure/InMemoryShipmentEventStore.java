package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentEventStore;
import delivery.domain.ShipmentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.Collections.synchronizedList;

@Adapter
public class InMemoryShipmentEventStore implements ShipmentEventStore {

    private final Map<String, List<ShipmentEvent>> store = new ConcurrentHashMap<>();

    @Override
    public void append(ShipmentEvent event) {
        store.computeIfAbsent(event.getShipmentId(), id -> synchronizedList(new ArrayList<>())).add(event);
    }

    @Override
    public List<ShipmentEvent> findByShipmentId(String shipmentId) {
        List<ShipmentEvent> events = store.get(shipmentId);
        return events != null ? new ArrayList<>(events) : List.of();
    }
}