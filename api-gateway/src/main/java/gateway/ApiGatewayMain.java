package gateway;

import gateway.infrastructure.HealthChecker;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import gateway.infrastructure.ApiGatewayController;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiGatewayMain {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("api-gateway").load(); //carica il .env
        String requestServiceUrl = dotenv.get("REQUEST_SERVICE_URL"); //legge il primo url
        String droneServiceUrl = dotenv.get("DRONE_SERVICE_URL"); //legge il secondo url
        String deliveryServiceUrl = dotenv.get("DELIVERY_SERVICE_URL"); //legge il terzo url

        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea il controller
        ApiGatewayController apiGatewayController = new ApiGatewayController(vertx, requestServiceUrl, deliveryServiceUrl);

        //crea l'health checker
        HealthChecker healthChecker = new HealthChecker(vertx, requestServiceUrl, droneServiceUrl, deliveryServiceUrl);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        apiGatewayController.registerRoutes(router);
        healthChecker.registerRoutes(router);

        //configura l'accesso all'interfaccia
        router.route("/ui/*").handler(StaticHandler.create("webroot"));

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Api gateway started on port {}", port);
    }
}