package delivery;

import delivery.application.DeliveryMetrics;
import delivery.application.ShipmentManager;
import delivery.application.ShipmentManagerImpl;
import delivery.application.ShipmentRepository;
import delivery.infrastructure.*;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("delivery-service").ignoreIfMissing().load();

        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = System.getenv("METRICS_PORT") != null ? Integer.parseInt(System.getenv("METRICS_PORT")) : Integer.parseInt(dotenv.get("METRICS_PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea il repository
        ShipmentRepository repository = new InMemoryShipmentRepository();

        DeliveryMetrics metrics = null;
        try {
            metrics = new DeliveryMetricsController(metricsPort);
            log.info("Prometheus metrics available on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics server: {}", e.getMessage());
        }

        //crea il manager
        ShipmentManager shipmentManager = new ShipmentManagerImpl(repository, metrics);

        //crea i controller
        ShipmentAssignmentController assignmentController = new ShipmentAssignmentController(shipmentManager);
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipmentManager);
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