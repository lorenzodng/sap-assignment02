package request.domain;

import buildingblocks.domain.AggregateRoot;
import java.time.LocalDate;
import java.time.LocalTime;

public class Shipment implements AggregateRoot<String> {

    private final String id;
    private final User user;
    private final Position pickupLocation;
    private final Position deliveryLocation;
    private final LocalDate pickupDate;
    private final LocalTime pickupTime;
    private final int deliveryTimeLimit;
    private final Package pack;

    public Shipment(String id, User user, Position pickupLocation, Position deliveryLocation, LocalDate pickupDate, LocalTime pickupTime, int deliveryTimeLimit, Package pack) {
        this.id = id;
        this.user = user;
        this.pickupLocation = pickupLocation;
        this.deliveryLocation = deliveryLocation;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.deliveryTimeLimit = deliveryTimeLimit;
        this.pack = pack;
    }

    @Override
    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Position getPickupLocation() {
        return pickupLocation;
    }

    public Position getDeliveryLocation() {
        return deliveryLocation;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public LocalTime getPickupTime() {
        return pickupTime;
    }

    public int getDeliveryTimeLimit() {
        return deliveryTimeLimit;
    }

    public Package getPackage() {
        return pack;
    }
}