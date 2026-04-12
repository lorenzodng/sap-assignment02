package gateway.infrastructure;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

//end-to-end test
@ExtendWith(VertxExtension.class)
class ShipmentUserJourneyTest {

    private WebClient client;
    private Vertx vertx;
    private final int GATEWAY_PORT = 8080;
    private static final int HTTP_CREATED = 201;
    private static final int HTTP_OK = 200;
    private static final int MAX_POLLING_ATTEMPTS = 10;
    private static final long POLLING_RETRY_DELAY_MS = 300;

    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;
        client = WebClient.create(vertx);
    }

    //verifica il flusso di utilizzo del sistema dalla richiesta di spedizione al tracking
    @Test
    void testCreationAndStatusJourney(VertxTestContext ctx) {
        JsonObject shipmentBody = createShipmentPayload();
        createShipmentRequest(shipmentBody, ctx);
    }

    //crea la richiesta di spedizione
    private void createShipmentRequest(JsonObject body, VertxTestContext ctx) {
        client.post(GATEWAY_PORT, "localhost", "/shipments").sendJsonObject(body)
                .onSuccess(response -> {
                    assertEquals(HTTP_CREATED, response.statusCode()); //verifica se la richiesta è stata elaborata correttamente dal server
                    String id = response.bodyAsString().trim(); //estrae l'id dalla richiesta
                    assertNotNull(id); //verifica se l'id non è nullo

                    waitForShipmentStatus(id, ctx, MAX_POLLING_ATTEMPTS); //controlla lo stato della consegna (tracking)
                })
                .onFailure(ctx::failNow); //se c'è un problema, fallisce il test immediatamente
    }

    //controlla lo stato della consegna
    private void waitForShipmentStatus(String shipmentId, VertxTestContext ctx, int remainingAttempts) {
        client.get(GATEWAY_PORT, "localhost", "/shipments/" + shipmentId + "/status")
                .send()
                .onSuccess(response -> {
                    if (response.statusCode() == HTTP_OK) {
                        JsonObject body = response.bodyAsJsonObject(); //recupera la risposta
                        assertEquals(shipmentId, body.getString("id")); //verifica se la risposta è legata alla richiesta emessa
                        assertNotNull(body.getString("status")); //verifica se lo stato non è nullo

                        ctx.completeNow(); //chiude il test
                    } else if (remainingAttempts > 1) {
                        vertx.setTimer(POLLING_RETRY_DELAY_MS, id -> waitForShipmentStatus(shipmentId, ctx, remainingAttempts - 1)); //esegue polling
                    } else {
                        ctx.failNow(new Throwable());
                    }
                })
                .onFailure(ctx::failNow);
    }

    //invia la richiesta di spedizione
    private JsonObject createShipmentPayload() {
        String userId = "mario-rossi-01";
        String name = "Mario";
        String surname = "Rossi";
        String date = "2020-01-01";
        String time = "15:00";
        int timeLimit = 60;
        double pickupLat = 41.90;
        double pickupLon = 12.49;
        double deliveryLat = 41.92;
        double deliveryLon = 12.51;
        double weight = 1.5;
        boolean isFragile = false;
        JsonObject pickupPos = new JsonObject().put("latitude", pickupLat).put("longitude", pickupLon);
        JsonObject deliveryPos = new JsonObject().put("latitude", deliveryLat).put("longitude", deliveryLon);
        JsonObject packageDetails = new JsonObject().put("weight", weight).put("fragile", isFragile);
        return new JsonObject().put("userId", userId).put("userName", name).put("userSurname", surname).put("pickupLocation", pickupPos).put("deliveryLocation", deliveryPos).put("pickupDate", date).put("pickupTime", time).put("deliveryTimeLimit", timeLimit).put("package", packageDetails);
    }
}