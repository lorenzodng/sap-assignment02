package delivery.application;

import buildingblocks.application.OutboundPort;

@OutboundPort
public interface DeliveryMetrics {
     void incrementCompleted();
     void incrementActive();
}
