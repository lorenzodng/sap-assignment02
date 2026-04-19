package delivery;

import delivery.application.*;
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
        String jaegerEndpoint = System.getenv("JAEGER_ENDPOINT") != null ? System.getenv("JAEGER_ENDPOINT") : dotenv.get("JAEGER_ENDPOINT");
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = System.getenv("METRICS_PORT") != null ? Integer.parseInt(System.getenv("METRICS_PORT")) : Integer.parseInt(dotenv.get("METRICS_PORT"));

        Vertx vertx = Vertx.vertx();

        TracingProvider tracingProvider = new TracingProvider(jaegerEndpoint, "delivery-service");
        TracingController tracingController = new TracingController(tracingProvider);

        ShipmentEventStore eventStore = new InMemoryShipmentEventStore();

        DeliveryMetrics metrics = null;
        try {
            metrics = new PrometheusDeliveryMetricsProxy(metricsPort);
            log.info("Prometheus metrics available on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics server: {}", e.getMessage());
        }

        ShipmentManager shipmentManager = new ShipmentManagerImpl(eventStore, metrics);

        ShipmentAssignmentController assignmentController = new ShipmentAssignmentController(shipmentManager);
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipmentManager);
        HealthController healthController = new HealthController();

        Router router = Router.router(vertx);
        tracingController.registerRoutes(router);
        assignmentController.registerRoutes(router);
        trackingController.registerRoutes(router);
        healthController.registerRoutes(router);

        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Delivery service started on port {}", port);
    }
}