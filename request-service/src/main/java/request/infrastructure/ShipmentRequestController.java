package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import request.application.CreateShipmentRequest;
import request.application.ValidateShipmentRequest;
import request.domain.Package;
import request.domain.Position;
import request.domain.Shipment;
import request.domain.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

//controller che riceve le richieste dal client
@Adapter
public class ShipmentRequestController {

    private final CreateShipmentRequest createShipmentRequest;
    private final ValidateShipmentRequest validateShipmentRequest;
    private final DroneServiceClient droneServiceClient;

    public ShipmentRequestController(CreateShipmentRequest createShipmentRequest, ValidateShipmentRequest validateShipmentRequest, DroneServiceClient droneServiceClient) {
        this.createShipmentRequest = createShipmentRequest;
        this.validateShipmentRequest = validateShipmentRequest;
        this.droneServiceClient = droneServiceClient;
    }

    //registra la rotta
    public void registerRoutes(Router router) {
        router.post("/shipments").handler(BodyHandler.create()).handler(this::createShipment); //quando arriva una richiesta sulla rotta "/shipments", invoca il metodo
    }

    //crea la richiesta
    private void createShipment(RoutingContext ctx) {
        var body = ctx.body().asJsonObject();

        //crea la richiesta
        User user = new User(body.getString("userId"), body.getString("userName"), body.getString("userSurname"));
        Position pickupLocation = new Position(body.getJsonObject("pickupLocation").getDouble("latitude"), body.getJsonObject("pickupLocation").getDouble("longitude"));
        Position deliveryLocation = new Position(body.getJsonObject("deliveryLocation").getDouble("latitude"), body.getJsonObject("deliveryLocation").getDouble("longitude"));
        Package pack = new Package(UUID.randomUUID().toString(), body.getJsonObject("package").getDouble("weight"), body.getJsonObject("package").getBoolean("fragile"));
        Shipment shipment = createShipmentRequest.create(UUID.randomUUID().toString(), user, pickupLocation, deliveryLocation, LocalDate.parse(body.getString("pickupDate")), LocalTime.parse(body.getString("pickupTime")), body.getInteger("deliveryTimeLimit"), pack);
        if (validateShipmentRequest.validate(shipment)) { //valida la richiesta
            droneServiceClient.notifyShipmentRequest(shipment).onSuccess(response -> {
                ctx.response().setStatusCode(response.statusCode()).putHeader("Content-Type", "application/json").end(shipment.getId());
            }).onFailure(err -> {
                ctx.response().setStatusCode(500).end("Error contacting drone service");
            });
        } else {
            ctx.response().setStatusCode(400).end("Invalid request");
        }
    }
}