package delivery.infrastructure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;

//configura l'instrumentation library
public class TracingProvider {

    private final Tracer tracer; //oggetto che crea gli span della richiesta

    public TracingProvider(String jaegerEndpoint, String serviceName) {
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder().setEndpoint(jaegerEndpoint).build(); //mittente che trasferisce le informazioni degli span
        Resource resource = Resource.getDefault().toBuilder().put(ServiceAttributes.SERVICE_NAME, serviceName).build(); //associa il nome del servizio agli span
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(BatchSpanProcessor.builder(exporter).build()).setResource(resource).build(); //gestore che raccoglie le informazioni degli span
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build(); //instrumentation library
        this.tracer = openTelemetry.getTracer(serviceName);
    }

    public Tracer getTracer() {
        return tracer;
    }
}