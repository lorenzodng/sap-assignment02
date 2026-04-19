package request.infrastructure;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import request.application.*;
import request.application.DroneServiceNotifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//component test
@ExtendWith(VertxExtension.class)
class ShipmentRequestComponentTest {

    private static final String HOST = "localhost";
    private static final int PORT = 8888;
    private static final int HTTP_CREATED = 201;
    private static final long MOCK_TIMER_ID = 1L;
    private WebClient client;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext ctx) {
        DroneServiceNotifier notifier = mock(DroneServiceNotifier.class);
        when(notifier.notifyShipmentRequest(any())).thenReturn(Future.succeededFuture());

        RequestMetrics metrics = mock(RequestMetrics.class);
        Vertx testVertx = mock(Vertx.class);
        when(testVertx.setTimer(anyLong(), any())).thenReturn(MOCK_TIMER_ID);

        ShipmentRequestOrchestrator orchestrator = new ShipmentRequestOrchestratorImpl(new CreateShipmentRequestImpl(), new ValidateShipmentRequestImpl(), new ShipmentSchedulerImpl(notifier, testVertx), metrics);

        ShipmentRequestController controller = new ShipmentRequestController(orchestrator);
        Router router = Router.router(vertx);
        controller.registerRoutes(router);

        vertx.createHttpServer().requestHandler(router).listen(PORT)
                .onSuccess(s -> {
                    client = WebClient.create(vertx);
                    ctx.completeNow();
                })
                .onFailure(ctx::failNow);
    }

    @Test
    void validShipmentRequestReturns201(VertxTestContext ctx) {
        String body = """
            {
                "userId": "user-1",
                "userName": "Mario",
                "userSurname": "Rossi",
                "pickupLocation": {"latitude": 41.90, "longitude": 12.49},
                "deliveryLocation": {"latitude": 41.92, "longitude": 12.51},
                "pickupDate": "2030-01-01",
                "pickupTime": "10:00",
                "deliveryTimeLimit": 60,
                "package": {"weight": 1.5, "fragile": false}
            }
            """;

        client.post(PORT, HOST, "/shipments").putHeader("Content-Type", "application/json").sendBuffer(Buffer.buffer(body))
                .onSuccess(response -> {
                    assertEquals(HTTP_CREATED, response.statusCode());
                    ctx.completeNow();
                })
                .onFailure(ctx::failNow);
    }
}