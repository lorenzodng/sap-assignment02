package gateway.infrastructure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;

public class TracingProvider {

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    public TracingProvider(String jaegerEndpoint, String serviceName) {
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder().setEndpoint(jaegerEndpoint).build();
        Resource resource = Resource.getDefault().toBuilder().put(ServiceAttributes.SERVICE_NAME, serviceName).build();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(BatchSpanProcessor.builder(exporter).build()).setResource(resource).build();
        openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();
        tracer = openTelemetry.getTracer(serviceName);
    }

    public OpenTelemetry getOpenTelemetry() {
        return openTelemetry;
    }

    public Tracer getTracer() {
        return tracer;
    }
}