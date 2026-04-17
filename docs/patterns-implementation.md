# Shipping on the Air — Architectural Implementation of Patterns

## Overview

This document describes the architectural patterns implemented in *Shipping on the Air*. The system is composed of four microservices — `api-gateway`, `request-service`, `drone-service`, `delivery-service` — each deployed as an independent Docker container.

The patterns covered are: API Gateway, Health Check API, Application Metrics, Event Sourcing, Circuit Breaker and Distributed Tracing.

---

## API Gateway

The API Gateway was introduced to decouple the client from the internal microservice topology, acting as single entry point for all client interactions: it receives HTTP requests from the frontend and forwards them to the appropriate microservices, shielding the client from the internal structure of the system.

The gateway is implemented in the `api-gateway` module, organised following clean architecture principles, with an application layer defining the ports, and an infrastructure layer hosting the concrete implementations of all its components.

The `ApiGatewayController`, in the infrastructure layer, handles routing for shipment creation (`POST /shipments`) and tracking (`GET /shipments/:id/status`, `GET /shipments/:id/position`, `GET /shipments/:id/remaining-time`), forwarding requests to `request-service` and `delivery-service` respectively using a non-blocking `WebClient`. 

The gateway also hosts cross-cutting infrastructure components — tracing, metrics, health aggregation, and circuit breakers — all registered on the same Vert.x router at startup.

---

## Health Check API

The Health Check API  pattern was introduced to provide the deployment infrastructure the visibility needed to detect and react to microservice failures automatically.

Each microservice exposes a `GET /health` endpoint implemented in the infrastructure layer (`HealthController`). The endpoint responds with HTTP 200 and `{ "status": "UP" }` when the service is operational, otherwise it is marked `DOWN`.  
The aggregated result is exposed at `GET /health` on the `api-gateway`, which aggregates the health of all microservices through `HealthCheckerController`, also in the infrastructure layer, by issuing parallel non-blocking requests to each service's `/health` endpoint using `Future.all()`.

On the deployment side, Docker is configured to periodically invoke each service's health endpoint via a `healthcheck` directive in the `docker-compose.yml`. 
If a service fails three consecutive checks, Docker marks the container as `unhealthy`.  
The `autoheal` container monitors all running containers and automatically restarts any that enter the `unhealthy` state.  
Additionally, `restart: on-failure` handles the case where a container crashes entirely.

---

## Application Metrics

Application Metrics were introduced to provide continuous visibility into the behaviour of each microservice.

Since microservices have distinct domain responsibilities, they implement their own domain responsibilities, each implements its own `*MetricsProxy` adapter in the infrastructure layer, exposing counters through a dedicated Prometheus metrics port that reflects its specific domain events.  
An important aspect of the design has been identifying the right metrics to expose, in order to allow a monitoring system to detect abnormal throughput patterns or degradation at any point in the delivery pipeline:

- The `api-gateway` tracks the number of shipment creation and tracking requests (`gateway_shipments_requests_total`), labelled by endpoint, HTTP method, and response status, via `PrometheusApiGatewayMetricsProxy`;
- The `request-service` tracks valid and invalid shipment requests (`request_shipments_validated_total`) via `PrometheusRequestMetricsProxy`;
- The `drone-service` tracks successful and failed drone assignments (`drone_assignments_completed_total`) via `PrometheusDroneMetricsProxy`;
- The `delivery-service` tracks completed deliveries and currently active ones (`delivery_completed_total`, `delivery_deliveries_active`) via `PrometheusDeliveryMetricsProxy`.

Prometheus scrapes these endpoints periodically as configured in `prometheus.yml`.

---

## Event Sourcing

Event Sourcing was introduced in `delivery-service` for tracking the full history of a shipment. 
This service was chosen because it is the only service whose state evolves through meaningful transitions over time, making the full event history essential for reconstructing the current state.

This is the exclusive persistence mechanism for shipment state: rather than storing the current state directly, the system appends domain events and reconstructs the state by replaying them.

The event store is modelled as the `ShipmentEventStore` port in the application layer, which defines two operations: `append` to persist a new event, and `findByShipmentId` to retrieve the full event history for a shipment.  
In the infrastructure layer, the `InMemoryShipmentEventStore` adapter implements the store using a `ConcurrentHashMap` of synchronised lists, ensuring thread-safe concurrent access.  
The current state of a shipment is never stored explicitly, but it is always derived by passing the event history to `Shipment.reconstitute(events)` in the domain layer, which replays the events to rebuild the aggregate.

Domain events are modelled as: `ShipmentAssigned` (drone assigned, delivery scheduled), `ShipmentCompleted` (delivery completed), and `ShipmentCancelled` (no drone available). These are produced by `ShipmentManagerImpl` in the application layer in response to assignment outcomes and delivery progress checks.

---

## Circuit Breaker

The Circuit Breaker pattern was introduced to prevent failures in downstream services from propagating to the client.

It is applied in the `api-gateway` to protect calls to `request-service` and `delivery-service`, which are the only two microservices directly reachable from the client.  
Two circuit breakers are instantiated at startup in the infrastructure layer using Resilience4j: `RequestServiceCircuitBreaker` and `DeliveryServiceCircuitBreaker`. 
Both share the same configuration, which defines a 50% failure rate threshold over a sliding window of 10 calls, a 30-second wait in the open state before transitioning to half-open, and the requirement of 3 successful calls required to return to closed.  

In `ApiGatewayController`, before each outbound call, the circuit breaker's `tryAcquirePermission()` is checked, allowing the gateway to handle each state accordingly:

- **Closed**: the call proceeds normally;
- **Half-open**: only a limited number of calls are permitted to test whether the
  downstream service has recovered;
- **Open**: the gateway immediately returns HTTP 503 without forwarding the request.

On success, `onSuccess()` updates the internal statistics; on failure or server error (HTTP 5xx), `onError()` increments the failure counter. 
This prevents cascading failures from propagating to the client when a downstream service is degraded.

---

## Distributed Tracing

Distributed Tracing was introduced to make the flow of each request visible across microservice boundaries, enabling latency issues to be diagnosed at the right level without inspecting each service in isolation.

It is implemented across all microservices using OpenTelemetry, each of which initialises, in the infrastructure layer, a `TracingProvider` that configures an OTLP gRPC exporter and registers the service name as a resource  attribute, and a `TracingController` responsible for creating and managing spans for each incoming request.

In the `api-gateway`, the controller creates a root span for each incoming request and constructs a `traceparent` header — formatted as `00-{traceId}-{spanId}-01` — which is propagated to downstream microservices via HTTP headers. 
In each microservice, the `TracingController` reads the incoming `traceparent` header, reconstructs the parent `SpanContext`, and links the new span to it, building a distributed trace hierarchy. 

The resulting traces are collected by Jaeger and visualised in its UI, making it possible to observe the end-to-end latency of each request as it propagates through the chain: `api-gateway` → `request-service` → `drone-service` → `delivery-service`.

---

## Sources

- Prometheus documentation — https://prometheus.io/docs/
- Resilience4j documentation — https://resilience4j.readme.io/
- OpenTelemetry documentation — https://opentelemetry.io/docs/
- Jaeger documentation — https://www.jaegertracing.io/docs/

