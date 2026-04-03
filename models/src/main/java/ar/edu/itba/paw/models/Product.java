package ar.edu.itba.paw.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Product {

    private final Long productId;
    private final Long userId;
    private final String title;
    private final String artist;
    private final List<Category> categories;
    private final String description;
    private final BigDecimal sleeveCondition;
    private final BigDecimal recordCondition;
    private final String neighborhood;
    private final String province;
    private final LocalDate published;
    private final BigDecimal price;

    public Product(
        final Long productId,
        final Long userId,
        final String title,
        final String artist,
        final List<Category> categories,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final String neighborhood,
        final String province,
        final LocalDate published,
        final BigDecimal price
    ) {
        this.productId = productId;
        this.userId = userId;
        this.title = title;
        this.artist = artist;
        this.categories = categories;
        this.description = description;
        this.sleeveCondition = sleeveCondition;
        this.recordCondition = recordCondition;
        this.neighborhood = neighborhood;
        this.province = province;
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

    public List<Category> getCategories() {
        return categories;
    }

    public BigDecimal getSleeveCondition() {
        return sleeveCondition;
    }

    public BigDecimal getRecordCondition() {
        return recordCondition;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getProvince() {
        return province;
    }

    public String getLocation() {
        return neighborhood + ", " + province;
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
