package ar.edu.itba.paw.models;

public class Category {

    private final Long id;
    private final String name;

    public Category(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
