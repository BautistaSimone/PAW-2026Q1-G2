package ar.edu.itba.paw.models;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categories_category_id_seq")
    @SequenceGenerator(sequenceName = "categories_category_id_seq", name = "categories_category_id_seq", allocationSize = 1)
    @Column(name = "category_id")
    private final Long id;

    @Column(length = 255, nullable = false)
    private final String name;

    Category() {
        // Just for Hibernate, we love you!
    }

    public Category(final String name) {
        this.name = name;
    }
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
