package drone;

import drone.application.*;
import drone.infrastructure.*;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import drone.domain.Drone;
import drone.domain.Position;
import java.util.ArrayList;
import java.util.List;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DroneServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("drone-service").ignoreIfMissing().load();
        String deliveryServiceUrl = System.getenv("DELIVERY_SERVICE_URL") != null ? System.getenv("DELIVERY_SERVICE_URL") : dotenv.get("DELIVERY_SERVICE_URL");
        String jaegerEndpoint = System.getenv("JAEGER_ENDPOINT") != null ? System.getenv("JAEGER_ENDPOINT") : dotenv.get("JAEGER_ENDPOINT");
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = System.getenv("METRICS_PORT") != null ? Integer.parseInt(System.getenv("METRICS_PORT")) : Integer.parseInt(dotenv.get("METRICS_PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea il tracing
        TracingProvider tracingProvider = new TracingProvider(jaegerEndpoint, "drone-service");
        TracingController tracingController = new TracingController(tracingProvider);

        //crea la flotta di droni (posizionati a Roma)
        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("drone-1", new Position(41.90, 12.49)));
        drones.add(new Drone("drone-2", new Position(41.91, 12.50)));
        drones.add(new Drone("drone-3", new Position(41.92, 12.51)));

        //crea i use case
        CheckDroneAvailability checker = new CheckDroneAvailabilityImpl();
        AssignDrone assigner = new AssignDroneImpl(checker);

        //crea il livello infrastruttura
        DroneRepository droneRepository = new InMemoryDroneRepository(drones);
        DeliveryServiceNotifier deliveryNotifier = new DeliveryServiceClient(vertx, deliveryServiceUrl);

        //crea i controller
        HealthController healthController = new HealthController();
        DroneMetrics metrics = null;
        try {
            metrics = new PrometheusDroneMetricsProxy(metricsPort);
            log.info("Prometheus metrics available on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics server: {}", e.getMessage());
        }

        //crea l'orchestratore
        DroneAssignmentOrchestrator orchestrator = new DroneAssignmentOrchestratorImpl(assigner, deliveryNotifier, droneRepository, metrics);

        //crea il controller
        DroneAssignmentController droneController = new DroneAssignmentController(orchestrator);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        tracingController.registerRoutes(router);
        droneController.registerRoutes(router);
        healthController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Drone service started on port {}", port);
    }
}