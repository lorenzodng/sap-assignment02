package request.application;

import request.domain.Shipment;

public interface ValidateShipmentRequest {
    void validate(Shipment shipment);
}