package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import gateway.application.ApiGatewayObserver;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

@Adapter
public class PrometheusController implements ApiGatewayObserver {

    private final Counter totalRESTRequests;
    private final Counter droneAssignments;
    private final Counter validShipmentRequests;

    public PrometheusController(int port) throws Exception {
        JvmMetrics.builder().register();
        totalRESTRequests = Counter.builder().name("gateway_rest_requests_total").help("Total number of REST requests received").labelNames("endpoint").register(); //numero di richieste di creazione e numero di richieste di tacking spedizione
        validShipmentRequests = Counter.builder().name("gateway_valid_shipment_requests_total").help("Total number of valid shipment requests").register(); //numero di richieste di creazione validate (campi giusti)
        droneAssignments = Counter.builder().name("gateway_drone_assignments_total").help("Total number of drone assignment outcomes").labelNames("outcome").register(); //numero di volte per cui un drone è stato assegnato e numero di volta per cui un drone non è stato assegnato
        HTTPServer.builder().port(port).buildAndStart();
    }

    @Override
    public void notifyShipmentRequest() {
        totalRESTRequests.labelValues("create").inc();
    }

    @Override
    public void notifyTrackingRequest() {
        totalRESTRequests.labelValues("tracking").inc();
    }

    @Override
    public void notifyDroneAvailable() {
        droneAssignments.labelValues("assigned").inc();
    }

    @Override
    public void notifyDroneNotAvailable() {
        droneAssignments.labelValues("not_available").inc();
    }

    @Override
    public void notifyValidShipmentRequest() {
        validShipmentRequests.inc();
    }
}