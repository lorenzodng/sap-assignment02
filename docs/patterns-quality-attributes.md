# Shipping on the Air — Observability & Quality Attribute Scenarios

## Overview

This document discusses how the observability patterns implemented in *Shipping on the Air* support the realisation of quality attribute scenarios defined for the system. 
Two concrete examples are provided, each mapping a pattern to a specific quality attribute.

---

## Example 1 — Availability via Health Check API

**Quality attribute**: The system must always be reachable even in case of minor failures.

**Scenario**:

| Element     | Description                                                                              |
|-------------|------------------------------------------------------------------------------------------|
| Source      | Docker deployment infrastructure                                                         |
| Stimulus    | A microservice becomes unresponsive due to an internal failure                           |
| Environment | System running under normal operating conditions                                         |
| Response    | The deployment infrastructure detects the failure and restarts the affected service      |
| Measure     | The service is restored and reachable within 90 seconds (3 failed checks × 30s interval) |

**How the pattern supports it**:

Each microservice exposes a dedicated health endpoint that reports its operational status. The api-gateway aggregates these individual statuses, exposing a unified view of the health of the entire system.

On the deployment side, Docker is configured to periodically invoke each service's health endpoint. If a service fails to respond correctly for three consecutive checks, the deployment infrastructure automatically restarts it, restoring availability without manual intervention.

This closes the full loop described by the Health Check API pattern: the service exposes its health, the deployment infrastructure periodically checks it, and takes corrective action when needed.

---

## Example 2 — Performance via Application Metrics and Distributed Tracing

**Quality attribute**: The system must respond in real time for tracking requests.

**Scenario**:

| Element     | Description                                                                                       |
|-------------|---------------------------------------------------------------------------------------------------|
| Source      | Operations team or automated monitoring system                                                    |
| Stimulus    | Increased request volume causes latency to grow beyond acceptable thresholds                      |
| Environment | System under load, all services running                                                           |
| Response    | The anomaly is detected through metrics and traced to the specific service causing the bottleneck |
| Measure     | The degraded component is identified within minutes, without requiring manual inspection of logs  |

**How the patterns support it**:

Application Metrics, implemented via Prometheus, are collected at each microservice level through a dedicated metrics port: 
- The api-gateway tracks the number of shipment creation and tracking requests;
- The request-service tracks valid and invalid shipment requests; 
- The drone-service tracks successful and failed drone assignments;
- The delivery-service tracks completed deliveries and currently active ones.

These metrics allow a monitoring system to detect abnormal throughput patterns or drops in successful operations, signalling potential performance degradation.

Distributed Tracing captures the end-to-end latency of each request as it propagates through the microservice chain (api-gateway → request-service → drone-service → delivery-service). 
When a performance issue arises, traces make it possible to isolate which service in the chain is responsible for the latency increase, providing the information needed to act quickly and precisely.

Together, the two patterns provide complementary observability: metrics surface *that* a problem exists, while traces reveal *where* it originates.