package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import gateway.application.ApiGatewayMetrics;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

//raccoglie le metriche di richiesta spedizione e tracking
@Adapter
public class ApiGatewayMetricsController implements ApiGatewayMetrics {

    private final Counter totalRequests;
    private final HTTPServer server;

    public ApiGatewayMetricsController(int port) throws Exception {
        JvmMetrics.builder().register(); //metriche dello stato della jvm
        totalRequests = Counter.builder().name("gateway_shipments_requests_total").help("Total number of REST requests received").labelNames("endpoint").register(); //metriche del numero di richieste di creazione spedizione e numero di richieste di tacking spedizione
        server = HTTPServer.builder().port(port).buildAndStart();  //espone le metriche su una porta dedicata
    }

    //incrementa la metrica
    @Override
    public void incrementRequest(String path, String method, int statusCode) {
        totalRequests.labelValues(path, method, String.valueOf(statusCode)).inc(); //i parametri sono le informazioni mostrate (path per il microservizio e method per il tipo di richiesta http)
    }

    //ferma il server e libera la porta
    public void stop() {
        server.close();
    }

}