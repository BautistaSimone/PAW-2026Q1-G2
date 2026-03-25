package ar.edu.itba.paw.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {

    private final Long productId;
    private final Long userId;
    private final String title;
    private final String artist;
    private final String genre;
    private final String description;
    private final String condition;
    private final LocalDate published;
    private final BigDecimal price;

    public Product(
        final Long productId,
        final Long userId,
        final String title,
        final String artist,
        final String genre,
        final String description,
        final String condition,
        final LocalDate published,
        final BigDecimal price
    ) {
        this.productId = productId;
        this.userId = userId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.description = description;
        this.condition = condition;
        this.published = published;
        this.price = price;
    }

    public Long getId() {
        return productId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public String getCondition() {
        return condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getInstallmentPrice() {
        return price.divide(BigDecimal.valueOf(3), 2, java.math.RoundingMode.HALF_UP);
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getPublished() {
        return published;
    }
}
