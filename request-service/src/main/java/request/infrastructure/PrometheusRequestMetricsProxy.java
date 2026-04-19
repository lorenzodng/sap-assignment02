package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import request.application.RequestMetrics;

@Adapter
public class PrometheusRequestMetricsProxy implements RequestMetrics {

    private final Counter validShipmentRequests;
    private final HTTPServer server;

    public PrometheusRequestMetricsProxy(int port) throws Exception {
        JvmMetrics.builder().register();
        validShipmentRequests = Counter.builder().name("request_shipments_validated_total").help("Total shipment requests validated").labelNames("result").register();
        server = HTTPServer.builder().port(port).buildAndStart();
    }

    @Override
    public void incrementValidation(boolean isValid) {
        validShipmentRequests.labelValues(isValid ? "valid" : "invalid").inc();
    }

    public void stop() {
        server.close();
    }
}