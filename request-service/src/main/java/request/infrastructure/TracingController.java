package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.opentelemetry.api.trace.*;
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

    //costruisce la sequenza di span
    private void trace(RoutingContext ctx) {
        String traceparent = ctx.request().getHeader("traceparent");
        String spanName = ctx.request().method().name() + " " + ctx.request().path(); //costruisce il nome dello span a partire dal metodo http e dal path
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName); //crea il builder dello span - serve per preparare lo span (es. aggiungere il padre, aggiungere attributi ...)
        if (traceparent != null) {
            String[] parts = traceparent.split("-"); //divide l'header (traceparent) nelle sue parti
            if (parts.length == 4) {
                SpanContext parentContext = SpanContext.createFromRemoteParent(parts[1], parts[2], TraceFlags.getSampled(), TraceState.getDefault()); //ricostruisce il contesto dello span padre
                spanBuilder.setParent(Context.root().with(Span.wrap(parentContext))); //collega il nuovo span (ancora non creato definitivamente) allo span padre
            }
        }
        Span span = spanBuilder.startSpan(); //crea e avvia lo span
        try (Scope scope = span.makeCurrent()) { //serve a garantire un ordine gerarchico nel caso siano creati sotto-span
            ctx.addEndHandler(v -> span.end()); //chiude lo span quando la risposta viene inviata al client
            ctx.next(); //passa la richiesta al prossimo handler (senza di questo il programma si bloccherebbe qui, perchè nel main è il primo a essere registrato alle rotte)
        }
    }
}