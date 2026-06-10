package ar.edu.itba.paw.models;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
//import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_productid_seq")
    @SequenceGenerator(sequenceName = "products_product_id_seq", name = "products_productid_seq", allocationSize = 1)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User seller;
    private String title;
    private String artist;

    @Column(name = "record_label", length = 255, nullable = false)
    private String recordLabel;

    @Column(name = "catalog_number", length = 255, nullable = false)
    private String catalogNumber;

    @Column(name = "edition_country", length = 255, nullable = false)
    private String editionCountry;

    // @BatchSize(size = 20)
    @ManyToMany
    @JoinTable(
        name = "products_categories", 
        joinColumns = { @JoinColumn(name = "product_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "category_id") }
    )
    private List<Category> categories;
    

    @Column(length = 255, nullable = false)
    private String description;

    @Column(name = "sleeve_condition")
    private BigDecimal sleeveCondition;

    @Column(name = "record_condition")
    private BigDecimal recordCondition;
    private LocalDate published;
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private int stock = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 32, nullable = false)
    private ProductState state = ProductState.ACTIVE;

    Product() {
        // Just for Hibernate, we love you!
        categories = new ArrayList<>();
    }

    public Product(
        Long productId,
        long userId,
        String title,
        String artist,
        String recordLabel,
        String catalogNumber,
        String editionCountry,
        List<Category> categories,
        String description,
        BigDecimal sleeveCondition,
        BigDecimal recordCondition,
        LocalDate published,
        BigDecimal price,
        int stock
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
        this.published = published;
        this.price = price;
        this.stock = stock;
    }

    public Product(
        long userId,
        String title,
        String artist,
        String recordLabel,
        String catalogNumber,
        String editionCountry,
        List<Category> categories,
        String description,
        BigDecimal sleeveCondition,
        BigDecimal recordCondition,
        LocalDate published,
        BigDecimal price,
        int stock
    ) {
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
        this.published = published;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return productId;
    }

    public long getUserId() {
        return userId;
    }

    public User getSeller() {
        return seller;
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

    /** Truncated to 2 decimal places for display (legacy DB values). */
    public BigDecimal getRecordConditionDisplay() {
        if (recordCondition == null) {
            return null;
        }
        return recordCondition.setScale(2, RoundingMode.DOWN);
    }

    /** Truncated to 2 decimal places for display (legacy DB values). */
    public BigDecimal getSleeveConditionDisplay() {
        if (sleeveCondition == null) {
            return null;
        }
        return sleeveCondition.setScale(2, RoundingMode.DOWN);
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

    public ProductState getState() {
        return state;
    }

    public void setState(ProductState state) {
        this.state = state;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public void setEditionCountry(String editionCountry) {
        this.editionCountry = editionCountry;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void setSleeveCondition(BigDecimal sleeveCondition) {
        this.sleeveCondition = sleeveCondition;
    }

    public void setRecordCondition(BigDecimal recordCondition) {
        this.recordCondition = recordCondition;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPublished(LocalDate published) {
        this.published = published;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
