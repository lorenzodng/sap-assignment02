package request;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import request.application.CreateShipmentRequestImpl;
import request.application.ValidateShipmentRequestImpl;
import request.infrastructure.DroneServiceClient;
import request.infrastructure.HealthController;
import request.infrastructure.ShipmentRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceMain {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("request-service").load(); //carica le variabili del file .env
        String droneServiceUrl = dotenv.get("DRONE_SERVICE_URL");
        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea i use case
        CreateShipmentRequestImpl createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequestImpl validateShipmentRequest = new ValidateShipmentRequestImpl();

        //crea il producer Kafka
        DroneServiceClient droneServiceClient = new DroneServiceClient(vertx, droneServiceUrl);

        //crea i controller
        ShipmentRequestController shipmentController = new ShipmentRequestController(createShipmentRequest, validateShipmentRequest, droneServiceClient);
        HealthController healthController = new HealthController();

        //crea il router e registra la rotta
        Router router = Router.router(vertx);
        shipmentController.registerRoutes(router);
        healthController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}