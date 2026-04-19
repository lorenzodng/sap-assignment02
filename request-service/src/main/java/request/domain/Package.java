package request.domain;

import buildingblocks.domain.Entity;

public class Package implements Entity<String> {

    private final String id;
    private final double weight;
    private final boolean fragile;

    public Package(String id, double weight, boolean fragile) {
        this.id = id;
        this.weight = weight;
        this.fragile = fragile;
    }

    @Override
    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }
}