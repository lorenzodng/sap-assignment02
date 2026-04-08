package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentRepository;
import delivery.domain.Shipment;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

//gestisce lo stato delle spedizioni in memoria
@Adapter
public class InMemoryShipmentRepository implements ShipmentRepository {

    private final Map<String, Shipment> storage = new ConcurrentHashMap<>(); //mappa che tiene traccia di tutte le spedizioni

    //salva o aggiorna una spedizione
    @Override
    public void save(Shipment shipment) {
        storage.put(shipment.getId(), shipment);
    }

    //recupera una spedizione tramite id
    @Override
    public Optional<Shipment> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }
}