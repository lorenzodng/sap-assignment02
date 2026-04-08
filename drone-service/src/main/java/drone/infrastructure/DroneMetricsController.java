package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DroneMetrics;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

//raccoglie le metriche di drone assegnato e non assegnato
@Adapter
public class DroneMetricsController implements DroneMetrics {

    private final Counter droneAssignments;
    private final HTTPServer server;

    public DroneMetricsController(int port) throws Exception {
        JvmMetrics.builder().register(); //metriche dello stato della jvm
        droneAssignments = Counter.builder().name("drone_assignments_completed_total").help("Total number of drone assignment outcomes").labelNames("outcome").register(); //metriche di drone assegnato e non assegnato
        server = HTTPServer.builder().port(port).buildAndStart(); //espone le metriche su una porta dedicata
    }

    //incrementa la metrica
    @Override
    public void incrementAssignment(boolean success) {
        droneAssignments.labelValues(success ? "assigned" : "unavailable").inc();
    }

    //ferma il server e libera la porta
    public void stop() {
        server.close();
    }
}