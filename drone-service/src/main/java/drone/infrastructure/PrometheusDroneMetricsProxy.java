package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DroneMetrics;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

@Adapter
public class PrometheusDroneMetricsProxy implements DroneMetrics {

    private final Counter droneAssignments;
    private final HTTPServer server;

    public PrometheusDroneMetricsProxy(int port) throws Exception {
        JvmMetrics.builder().register();
        droneAssignments = Counter.builder().name("drone_assignments_completed_total").help("Total number of drone assignment outcomes").labelNames("outcome").register();
        server = HTTPServer.builder().port(port).buildAndStart();
    }

    @Override
    public void incrementAssignment(boolean success) {
        droneAssignments.labelValues(success ? "assigned" : "unavailable").inc();
    }

    public void stop() {
        server.close();
    }
}