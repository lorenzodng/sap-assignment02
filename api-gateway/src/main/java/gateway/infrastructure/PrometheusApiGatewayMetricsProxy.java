package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import gateway.application.ApiGatewayMetrics;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

@Adapter
public class PrometheusApiGatewayMetricsProxy implements ApiGatewayMetrics {

    private final Counter totalRequests;
    private final HTTPServer server;

    public PrometheusApiGatewayMetricsProxy(int port) throws Exception {
        JvmMetrics.builder().register();
        totalRequests = Counter.builder().name("gateway_shipments_requests_total").help("Total number of REST requests received").labelNames("endpoint", "method", "status").register();
        server = HTTPServer.builder().port(port).buildAndStart();
    }

    @Override
    public void incrementRequest(String path, String method, int statusCode) {
        totalRequests.labelValues(path, method, String.valueOf(statusCode)).inc();
    }

    public void stop() {
        server.close();
    }

}