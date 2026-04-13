package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentManager;
import delivery.application.ShipmentNotFoundException;
import delivery.domain.Position;
import delivery.domain.ShipmentStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import delivery.domain.Shipment;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//traccia la spedizione (stato e posizione del drone) e lo stato della spedizione
@Adapter
public class TrackingDeliveryController {

    private static final Logger log = LoggerFactory.getLogger(TrackingDeliveryController.class);
    private final ShipmentManager shipmentManager;

    public TrackingDeliveryController(ShipmentManager shipmentManager) {
        this.shipmentManager = shipmentManager;
    }

    //registra le rotte
    public void registerRoutes(Router router) {
        router.get("/shipments/:id/status").handler(this::getShipmentStatus);
        router.get("/shipments/:id/position").handler(this::getDronePosition);
        router.get("/shipments/:id/remaining-time").handler(this::getRemainingTime);
    }

    //recupera lo stato della spedizione
    private void getShipmentStatus(RoutingContext ctx) {
        try {
            String id = ctx.pathParam("id"); //estrae l'id dall'url del messaggio http
            shipmentManager.checkAndCompleteShipment(id); //controlla se la spedizione è stata completata
            Shipment shipment = shipmentManager.getShipmentDetails(id); //recupera la spedizione dalla mappa

            //costruisce il messaggio json
            JSONObject response = new JSONObject();
            ShipmentStatus status = shipment.updateStatus();
            log.info("Shipment {} status: {}", id, status.name());
            response.put("id", id);
            response.put("status", status.name());
            if (status == ShipmentStatus.CANCELLED) { //se lo stato della richiesta è CANCELLED
                response.put("message", "No drone available for this shipment");
            }
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(response.toString());
        } catch (ShipmentNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        } catch (Exception e) {
            ctx.response().setStatusCode(500).end("Internal Server Error");
        }
    }

    //recupera la posizione del drone
    private void getDronePosition(RoutingContext ctx) {
        try {
            String id = ctx.pathParam("id"); //estrae l'id dall'url del messaggio http
            shipmentManager.checkAndCompleteShipment(id); //controlla se la spedizione è stata completata
            Shipment shipment = shipmentManager.getShipmentDetails(id); //recupera la spedizione dalla mappa
            Position currentPosition = shipment.calculateCurrentDronePosition();
            if (currentPosition != null) { //se il drone è stato assegnato
                log.info("Delivery {} drone position: {}, {}", id, currentPosition.getLatitude(), currentPosition.getLongitude());
                JSONObject position = new JSONObject();
                position.put("id", id);
                position.put("latitude", currentPosition.getLatitude());
                position.put("longitude", currentPosition.getLongitude());
                ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(position.toString());
            } else {
                ctx.response().setStatusCode(400).end("Position not available: Shipment cancelled or not started");
            }
        } catch (ShipmentNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        } catch (Exception e) {
            ctx.response().setStatusCode(500).end("Internal Server Error");
        }
    }

    //recupera il tempo rimanente alla consegna
    private void getRemainingTime(RoutingContext ctx) {
        try {
            String id = ctx.pathParam("id"); //estrae l'id dall'url del messaggio http
            shipmentManager.checkAndCompleteShipment(id); //controlla se la spedizione è stata completata
            Shipment shipment = shipmentManager.getShipmentDetails(id); //recupera la spedizione dalla mappa
            double remainingMinutes = shipment.calculateRemainingTime(); //calcola il tempo rimanente
            log.info("Delivery {} remaining time: {} minutes", id, (int) remainingMinutes);

            //costruisce il messaggio json
            JSONObject response = new JSONObject();
            response.put("id", id);
            response.put("remainingMinutes", remainingMinutes);
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(response.toString()); //costruisce il messaggio di risposta e lo invia all'api-gateway
        } catch (ShipmentNotFoundException e) {
            ctx.response().setStatusCode(404).end(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error for shipment {}", ctx.pathParam("id"), e);
            ctx.response().setStatusCode(500).end("Internal Server Error");
        }
    }
}