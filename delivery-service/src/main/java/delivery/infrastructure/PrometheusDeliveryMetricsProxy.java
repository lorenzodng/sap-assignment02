package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.DeliveryMetrics;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

//raccoglie le metriche di consegne completate e consegne in corso
@Adapter
public class PrometheusDeliveryMetricsProxy implements DeliveryMetrics {

    private final Counter completedDeliveries;
    private final Gauge activeDeliveries;
    private final HTTPServer server;

    public PrometheusDeliveryMetricsProxy(int port) throws Exception {
        JvmMetrics.builder().register(); //metriche dello stato della jvm
        completedDeliveries = Counter.builder().name("delivery_completed_total").help("Total number of completed deliveries").register(); //metrica consegna completata
        activeDeliveries = Gauge.builder().name("delivery_deliveries_active").help("Number of shipments currently in progress").register(); //metrica consegna in corso
        server = HTTPServer.builder().port(port).buildAndStart();  //espone le metriche su una porta dedicata
    }

    //incrementa e decrementa le metriche quando una consegna è stata completata
    @Override
    public void incrementCompleted() {
        completedDeliveries.inc();
        activeDeliveries.dec();
    }

    //incrementa la metrica di consegna in corso
    @Override
    public void incrementActive() {
        activeDeliveries.inc();
    }

    //ferma il server e libera la porta
    public void stop() {
        server.close();
    }
}