package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@Adapter
public class TracingController {

    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;
    private static final TextMapGetter<HttpServerRequest> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(HttpServerRequest carrier) {
            return carrier.headers().names();
        }

        @Override
        public String get(HttpServerRequest carrier, String key) {
            return carrier.getHeader(key);
        }
    };

    public TracingController(TracingProvider tracingProvider) {
        this.tracer = tracingProvider.getTracer();
        this.openTelemetry = tracingProvider.getOpenTelemetry();
    }

    public void registerRoutes(Router router) {
        router.route().handler(this::trace);
    }

    private void trace(RoutingContext ctx) {
        String spanName = ctx.request().method().name() + " " + ctx.request().path();
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName);

        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), ctx.request(), GETTER);
        spanBuilder.setParent(extractedContext);
        Span span = spanBuilder.startSpan();
        try (Scope scope = span.makeCurrent()) {
            Vertx.currentContext().put("otelContext", Context.current());
            ctx.addEndHandler(v -> span.end());
            ctx.next();
        }
    }
}