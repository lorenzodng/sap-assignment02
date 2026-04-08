package gateway.application;

import buildingblocks.application.OutboundPort;

@OutboundPort
public interface ApiGatewayMetrics {

    void incrementRequest(String path, String method, int statusCode);
}