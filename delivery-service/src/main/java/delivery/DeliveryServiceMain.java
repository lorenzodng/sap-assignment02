package delivery;

import delivery.infrastructure.HealthController;
import delivery.infrastructure.ShipmentAssignment;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import delivery.domain.Shipment;
import delivery.infrastructure.TrackingDeliveryController;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("delivery-service").load(); //carica le variabili del file .env
        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea i consumer Kafka
        Map<String, Shipment> shipments = new HashMap<>();
        ShipmentAssignment assignmentController = new ShipmentAssignment(shipments);

        //crea i controller REST
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipments);
        HealthController healthController = new HealthController();

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        assignmentController.registerRoutes(router);
        trackingController.registerRoutes(router);
        healthController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Delivery service started on port {}", port);
    }
}