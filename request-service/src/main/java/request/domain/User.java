package request.domain;

import buildingblocks.domain.Entity;

public class User implements Entity<String> {

    private String id;
    private String name;
    private String surname;

    public User(String id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}