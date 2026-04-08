package request;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import request.application.*;
import request.infrastructure.DroneServiceClient;
import request.infrastructure.HealthController;
import request.infrastructure.RequestMetricsController;
import request.infrastructure.ShipmentRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceMain {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("request-service").load(); //carica le variabili del file .env
        String droneServiceUrl = dotenv.get("DRONE_SERVICE_URL");
        int port = Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = Integer.parseInt(dotenv.get("METRICS_PORT")); //legge la porta per Prometheus

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea i use case
        CreateShipmentRequest createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequest validateShipmentRequest = new ValidateShipmentRequestImpl();
        ShipmentScheduler shipmentScheduler = new ShipmentSchedulerImpl();

        //crea il producer
        DroneServiceNotifier droneServiceNotifier = new DroneServiceClient(vertx, droneServiceUrl);

        //crea i controller
        HealthController healthController = new HealthController();
        RequestMetrics metrics = null;
        try {
            metrics = new RequestMetricsController(metricsPort);
            log.info("Prometheus metrics available on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics server: {}", e.getMessage());
        }

        //crea l'orchestratore
        ShipmentRequestOrchestrator orchestrator = new ShipmentRequestOrchestratorImpl(createShipmentRequest, validateShipmentRequest, shipmentScheduler, metrics);

        //crea il controller
        ShipmentRequestController shipmentController = new ShipmentRequestController(orchestrator, droneServiceNotifier);

        //crea il router e registra la rotta
        Router router = Router.router(vertx);
        shipmentController.registerRoutes(router);
        healthController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}