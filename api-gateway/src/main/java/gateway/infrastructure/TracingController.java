package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

//crea gli span e gestisce la loro esecuzione
@Adapter
public class TracingController {

    private final Tracer tracer;

    public TracingController(TracingProvider tracingProvider) {
        this.tracer = tracingProvider.getTracer();
    }

    public void registerRoutes(Router router) {
        router.route().handler(this::trace);
    }

    //crea e avvia lo span
    private void trace(RoutingContext ctx) {
        String spanName = ctx.request().method().name() + " " + ctx.request().path(); //costruisce il nome dello span a partire dal metodo http e dal path
        Span span = tracer.spanBuilder(spanName).startSpan(); //crea e avvia lo span
        try (Scope scope = span.makeCurrent()) { //imposta lo span come span corrente
            ctx.put("otelContext", Context.current()); //salva il contesto dello span in vertx in modo che non vada perso
            ctx.addEndHandler(v -> span.end()); //chiude lo span quando la risposta viene inviata al client
            ctx.next();
        }
    }
}