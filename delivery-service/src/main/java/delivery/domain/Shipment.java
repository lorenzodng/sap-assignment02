package delivery.domain;

import buildingblocks.domain.AggregateRoot;
import java.util.List;

public class Shipment implements AggregateRoot<String> {

    private static final double MS_TO_HOURS = 3600000.0;
    private static final int MINUTES_IN_HOUR = 60;
    private final String id;
    private Position droneInitialPosition;
    private Position pickupPosition;
    private Position deliveryPosition;
    private long assignedAt;
    private double deliverySpeed;
    private ShipmentStatus status;

    public Shipment(String id, Position droneInitialPosition, Position pickupPosition, Position deliveryPosition, long assignedAt, double deliverySpeed) {
        this.id = id;
        this.droneInitialPosition = droneInitialPosition;
        this.pickupPosition = pickupPosition;
        this.deliveryPosition = deliveryPosition;
        this.assignedAt = assignedAt;
        this.deliverySpeed = deliverySpeed;
        this.status = ShipmentStatus.SCHEDULED;
    }

    public Position calculateCurrentDronePosition() {

        if (droneInitialPosition == null) {
            return null;
        }

        double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS;
        double distanceCovered = deliverySpeed * elapsedHours;

        //phase 1: drone moves toward the pickup location
        double distanceToPickup = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude());
        if (distanceCovered < distanceToPickup) {
            return interpolate(droneInitialPosition, pickupPosition, distanceCovered / distanceToPickup);
        }

        //phase 2: drone moves toward the delivery destination
        double distanceCovered2 = distanceCovered - distanceToPickup;
        double distanceToDelivery = GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude());
        if (distanceCovered2 < distanceToDelivery) {
            return interpolate(pickupPosition, deliveryPosition, distanceCovered2 / distanceToDelivery);
        }

        return deliveryPosition;
    }

    public double calculateRemainingTime() {

        if (droneInitialPosition == null) {
            return 0;
        }
        double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS;
        double distanceCovered = deliverySpeed * elapsedHours;
        double distanceToPickup = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude());
        double distanceToDelivery = GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude());
        double totalDistance = distanceToPickup + distanceToDelivery;
        double remainingDistance = Math.max(0, totalDistance - distanceCovered);
        return (int) Math.ceil((remainingDistance / deliverySpeed) * MINUTES_IN_HOUR);
    }

    public ShipmentStatus updateStatus() {
        if (droneInitialPosition != null) {
            double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS;
            double distanceCovered = deliverySpeed * elapsedHours;
            double distanceToPickup = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude());
            double distanceToDelivery = GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude());
            double totalDistance = distanceToPickup + distanceToDelivery;
            if (distanceCovered >= totalDistance) {
                this.status = ShipmentStatus.COMPLETED;
            } else if (distanceCovered >= distanceToPickup) {
                this.status = ShipmentStatus.IN_PROGRESS;
            }
        }
        return status;
    }

    @Override
    public String getId() {
        return id;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    private Position interpolate(Position from, Position to, double fraction) {
        double lat = from.getLatitude() + (to.getLatitude() - from.getLatitude()) * fraction;
        double lon = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * fraction;
        return new Position(lat, lon);
    }

    //for event sourcing

    public static Shipment reconstitute(List<ShipmentEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Cannot reconstitute shipment: no events provided");
        }
        Shipment shipment = null;
        for (ShipmentEvent event : events) {
            if (event instanceof ShipmentAssigned e) {
                shipment = new Shipment(e.getShipmentId(), e.getDroneInitialPosition(), e.getPickupPosition(), e.getDeliveryPosition(), e.getOccurredAt(), e.getDeliverySpeed());
            } else if (event instanceof ShipmentCompleted) {
                if (shipment != null) {
                    shipment.status = ShipmentStatus.COMPLETED;
                }
            } else if (event instanceof ShipmentCancelled) {
                if (shipment != null) {
                    shipment.status = ShipmentStatus.CANCELLED;
                }
            }
        }

        if (shipment == null) {
            throw new IllegalStateException("Reconstitution failed: ShipmentAssigned event missing");
        }

        return shipment;
    }
}