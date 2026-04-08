package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import request.application.DroneServiceNotifier;
import request.domain.Shipment;

//client che notifica la creazione della richiesta di spedizione verso drone-service
@Adapter
public class DroneServiceClient implements DroneServiceNotifier {

    private static final Logger log = LoggerFactory.getLogger(DroneServiceClient.class);
    private final WebClient client;
    private final String droneServiceUrl;

    public DroneServiceClient(Vertx vertx, String droneServiceUrl) {
        this.client = WebClient.create(vertx);
        this.droneServiceUrl = droneServiceUrl;
    }

    /*
    1) l'utente invoca l'api-gateway che contatta request-service (in ShipmentRequestController)
    2) request-service aspetta la risposta di stato da drone-service per sapere cosa rispondere all'api-gateway (con "createShipment" in ShipmentRequestController)
    3) l'api-gateway mostra il messaggio all'utente
     */
    @Override
    public Future<Void> notifyShipmentRequest(Shipment shipment) {
        JSONObject body = new JSONObject();
        body.put("shipmentId", shipment.getId());
        body.put("pickupLatitude", shipment.getPickupLocation().getLatitude());
        body.put("pickupLongitude", shipment.getPickupLocation().getLongitude());
        body.put("deliveryLatitude", shipment.getDeliveryLocation().getLatitude());
        body.put("deliveryLongitude", shipment.getDeliveryLocation().getLongitude());
        body.put("packageWeight", shipment.getPackage().getWeight());
        body.put("deliveryTimeLimit", shipment.getDeliveryTimeLimit());

        return client.postAbs(droneServiceUrl + "/shipments/assign").putHeader("Content-Type", "application/json").sendBuffer(Buffer.buffer(body.toString()))
                .onSuccess(res -> log.info("Shipment {} request notified", shipment.getId()))
                .onFailure(err -> log.error("Failed to notify drone service for shipment {}", shipment.getId(), err))
                .mapEmpty(); //trasforma il risultato in Future<Void>
    }
}