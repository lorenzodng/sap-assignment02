package request.application;

import buildingblocks.application.InboundPort;
import request.domain.Shipment;

@InboundPort
public interface ValidateShipmentRequest {
    boolean validate(Shipment shipment);
}