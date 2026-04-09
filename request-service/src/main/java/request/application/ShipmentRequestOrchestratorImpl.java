package request.application;

import io.vertx.core.Future;
import request.domain.Shipment;

//orchestratore che coordina il flusso principale di gestione di una richiesta
public class ShipmentRequestOrchestratorImpl implements ShipmentRequestOrchestrator {

    private final CreateShipmentRequest creator;
    private final ValidateShipmentRequest validator;
    private final ShipmentScheduler scheduler;
    private final RequestMetrics metrics;

    public ShipmentRequestOrchestratorImpl(CreateShipmentRequest creator, ValidateShipmentRequest validator, ShipmentScheduler scheduler, RequestMetrics metrics) {
        this.creator = creator;
        this.validator = validator;
        this.scheduler = scheduler;
        this.metrics = metrics;
    }

    @Override
    public Future<Shipment> orchestrateRequest(String userId, String userName, String userSurname, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, String pickupDate, String pickupTime, Integer deliveryTimeLimit, Double weight, Boolean fragile) {

        //step 1: crea la richiesta
        Shipment shipment = creator.create(userId, userName, userSurname, pickupLat, pickupLon, deliveryLat, deliveryLon, pickupDate, pickupTime, deliveryTimeLimit, weight, fragile);
        if (shipment != null && validator.validate(shipment)) {
            metrics.incrementValidation(true);

            //step 2: verifica il tempo trascorso e notifica drone-service
            return scheduler.schedule(shipment).map(v -> shipment); //trasforma il valore di ritorno in una Future
        } else {
            metrics.incrementValidation(false);
            return Future.failedFuture("VALIDATION_FAILED"); //se si verificano errori in fase di creazione o validazione
        }
    }
}