package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@Adapter
public class TracingController {

    private final Tracer tracer;

    public TracingController(TracingProvider tracingProvider) {
        this.tracer = tracingProvider.getTracer();
    }

    public void registerRoutes(Router router) {
        router.route().handler(this::trace);
    }

    private void trace(RoutingContext ctx) {
        String spanName = ctx.request().method().name() + " " + ctx.request().path();
        Span span = tracer.spanBuilder(spanName).startSpan();
        try (Scope scope = span.makeCurrent()) {
            ctx.put("otelContext", Context.current());
            ctx.addEndHandler(v -> span.end());
            ctx.next();
        }
    }
}