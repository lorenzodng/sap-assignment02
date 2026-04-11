package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
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
        String spanName = ctx.request().method().name() + " " + ctx.request().path(); //costruisce il nome dello span a partire dal metodo http e dal path
        Span span = tracer.spanBuilder(spanName).startSpan(); //crea e avvia lo span
        try (Scope scope = span.makeCurrent()) { //serve a garantire un ordine gerarchico nel caso siano creati sotto-span
            String traceparent = span.getSpanContext().isValid() ? "00-" + span.getSpanContext().getTraceId() + "-" + span.getSpanContext().getSpanId() + "-01" : null; //se lo span è stato creato correttamente, costruisce la stringa traceparent, altrimenti restituisce null
            if (traceparent != null) { //se traceparent è stato creato correttamente
                ctx.put("traceparent", traceparent); //memorizza il traceparent nel contesto della richiesta
            }
            ctx.addEndHandler(v -> span.end()); //chiude lo span quando la risposta viene inviata al client
            ctx.next(); //passa la richiesta al prossimo handler (senza di questo il programma si bloccherebbe qui, perchè nel main è il primo a essere registrato alle rotte)
        }
    }
}