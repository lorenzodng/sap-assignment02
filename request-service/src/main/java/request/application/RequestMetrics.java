package request.application;

import buildingblocks.application.OutboundPort;

@OutboundPort
public interface RequestMetrics {
    void incrementValidation(boolean isValid);
}