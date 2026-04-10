package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import request.application.RequestMetrics;

//raccoglie le metriche di richieste validate e non validate
@Adapter
public class PrometheusRequestMetricsProxy implements RequestMetrics {

    private final Counter validShipmentRequests;
    private final HTTPServer server;

    public PrometheusRequestMetricsProxy(int port) throws Exception {
        JvmMetrics.builder().register(); //metriche dello stato della jvm
        validShipmentRequests = Counter.builder().name("request_shipments_validated_total").help("Total shipment requests validated").labelNames("result").register(); //metriche di richieste validate e non validate
        server = HTTPServer.builder().port(port).buildAndStart(); //espone le metriche su una porta dedicata
    }

    //incrementa la metrica
    @Override
    public void incrementValidation(boolean isValid) {
        validShipmentRequests.labelValues(isValid ? "valid" : "invalid").inc();
    }

    //ferma il server e libera la porta
    public void stop() {
        server.close();
    }
}