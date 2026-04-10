package request.application;

import request.domain.Shipment;

public interface ValidateShipmentRequest {
    boolean validate(Shipment shipment);
}