package drone.domain;

import buildingblocks.domain.Entity;

public class Drone implements Entity<String> {

    public static final double BATTERY_CONSUMPTION_PER_KM = 2.0;
    public static final double SAFETY_BATTERY = 10.0;
    public static final double SPEED = 50.0;
    public static final double INITIAL_BATTERY = 100.0;
    public static final double WEIGHT_CAPACITY = 5.0;
    private final String id;
    private double batteryCapacity;
    private Position position;
    private boolean available;

    public Drone(String id, Position position) {
        this.id = id;
        this.batteryCapacity = INITIAL_BATTERY;
        this.position = position;
        this.available = true;
    }

    @Override
    public String getId() {
        return id;
    }

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isAvailable() {
        return available;
    }

    public void updatePosition(Position newPosition) {
        this.position = newPosition;
    }

    public void updateBattery(double consumedBattery) {
        this.batteryCapacity -= consumedBattery;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}