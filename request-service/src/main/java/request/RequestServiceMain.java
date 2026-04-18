package request;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import request.application.*;
import request.infrastructure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceMain {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("request-service").ignoreIfMissing().load();
        String droneServiceUrl = System.getenv("DRONE_SERVICE_URL") != null ? System.getenv("DRONE_SERVICE_URL") : dotenv.get("DRONE_SERVICE_URL");
        String jaegerEndpoint = System.getenv("JAEGER_ENDPOINT") != null ? System.getenv("JAEGER_ENDPOINT") : dotenv.get("JAEGER_ENDPOINT");
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = System.getenv("METRICS_PORT") != null ? Integer.parseInt(System.getenv("METRICS_PORT")) : Integer.parseInt(dotenv.get("METRICS_PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea il tracing
        TracingProvider tracingProvider = new TracingProvider(jaegerEndpoint, "request-service");
        TracingController tracingController = new TracingController(tracingProvider);

        //crea il producer
        DroneServiceNotifier droneServiceNotifier = new DroneServiceClient(vertx, droneServiceUrl, tracingProvider.getOpenTelemetry());

        //crea i use case
        CreateShipmentRequest createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequest validateShipmentRequest = new ValidateShipmentRequestImpl();
        ShipmentScheduler shipmentScheduler = new ShipmentSchedulerImpl(droneServiceNotifier, vertx);

        //crea i controller
        HealthController healthController = new HealthController();
        RequestMetrics metrics = null;
        try {
            metrics = new PrometheusRequestMetricsProxy(metricsPort);
            log.info("Prometheus metrics available on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics server: {}", e.getMessage());
        }

        //crea l'orchestratore
        ShipmentRequestOrchestrator orchestrator = new ShipmentRequestOrchestratorImpl(createShipmentRequest, validateShipmentRequest, shipmentScheduler, metrics);

        //crea il controller
        ShipmentRequestController shipmentController = new ShipmentRequestController(orchestrator);

        //crea il router e registra la rotta
        Router router = Router.router(vertx);
        tracingController.registerRoutes(router);
        shipmentController.registerRoutes(router);
        healthController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}