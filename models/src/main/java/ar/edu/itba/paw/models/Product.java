package ar.edu.itba.paw.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Product {

    private final Long id;
    private final String title;
    private final String artist;
    private final String genre;
    private final String vinylCondition;
    private final BigDecimal price;
    private final String imageUrl;
    private final String description;
    private final LocalDate publishedDate;

    public Product(
        final Long id,
        final String title,
        final String artist,
        final String genre,
        final String vinylCondition,
        final BigDecimal price,
        final String imageUrl,
        final String description,
        final LocalDate publishedDate
    ) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.vinylCondition = vinylCondition;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.publishedDate = publishedDate;
    }

    public Long getId() {
        return id;
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

    public String getVinylCondition() {
        return vinylCondition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getInstallmentPrice() {
        return price.divide(BigDecimal.valueOf(3), 2, java.math.RoundingMode.HALF_UP);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }
}
