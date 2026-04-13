package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentEventStore;
import delivery.domain.ShipmentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.Collections.synchronizedList;

//event store
@Adapter
public class InMemoryShipmentEventStore implements ShipmentEventStore {

    private final Map<String, List<ShipmentEvent>> store = new ConcurrentHashMap<>(); //mappa che tiene traccia di tutti gli eventi legati a una spedizione

    //aggiunge un evento alla mappa
    @Override
    public void append(ShipmentEvent event) {
        //si usa synchronized per gestire accessi atomici alla lista interna una volta restituita dalla mappa
        store.computeIfAbsent(event.getShipmentId(), id -> synchronizedList(new ArrayList<>())).add(event); //se non esiste una lista di eventi per una spedizione, crea la lista e aggiunge l'evento
    }

    //restituisce gli eventi di una spedizione
    @Override
    public List<ShipmentEvent> findByShipmentId(String shipmentId) {
        //viene creata e restituita una copia della lista per evitare che chi legge la lista possa interferire con chi la sta scrivendo
        List<ShipmentEvent> events = store.get(shipmentId);
        return events != null ? new ArrayList<>(events) : List.of();
    }
}