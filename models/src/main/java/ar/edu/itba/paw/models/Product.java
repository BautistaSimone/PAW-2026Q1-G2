package ar.edu.itba.paw.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Product {

    private final Long productId;
    private final Long userId;
    private final String title;
    private final String artist;
    private final String recordLabel;
    private final String catalogNumber;
    private final String editionCountry;
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
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
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
        this.recordLabel = recordLabel;
        this.catalogNumber = catalogNumber;
        this.editionCountry = editionCountry;
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

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getRecordLabel() {
        return recordLabel;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public String getEditionCountry() {
        return editionCountry;
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
