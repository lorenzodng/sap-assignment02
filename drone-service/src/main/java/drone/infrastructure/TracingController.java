package drone.infrastructure;

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

//crea gli span e gestisce la loro esecuzione
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
    }; //definisce come leggere gli header HTTP per estrarre il contesto di tracing

    public TracingController(TracingProvider tracingProvider) {
        this.tracer = tracingProvider.getTracer();
        this.openTelemetry = tracingProvider.getOpenTelemetry();
    }

    public void registerRoutes(Router router) {
        router.route().handler(this::trace);
    }

    //costruisce la sequenza di span
    private void trace(RoutingContext ctx) {
        String spanName = ctx.request().method().name() + " " + ctx.request().path(); //costruisce il nome dello span a partire dal metodo http e dal path
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName); //crea il builder dello span - serve per preparare lo span (es. aggiungere il padre, aggiungere attributi ...)

        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), ctx.request(), GETTER); //estrae il contesto di tracing dagli header in ingresso
        spanBuilder.setParent(extractedContext); //collega il nuovo span (ancora non creato definitivamente) allo span padre
        Span span = spanBuilder.startSpan(); //crea e avvia lo span
        try (Scope scope = span.makeCurrent()) { //imposta lo span come span corrente
            Vertx.currentContext().put("otelContext", Context.current()); //salva il contesto OTel nel contesto Vert.x
            ctx.addEndHandler(v -> span.end()); //chiude lo span quando la risposta viene inviata al client
            ctx.next(); //passa la richiesta al prossimo handler (senza di questo il programma si bloccherebbe qui, perchè nel main è il primo a essere registrato alle rotte)
        }
    }
}