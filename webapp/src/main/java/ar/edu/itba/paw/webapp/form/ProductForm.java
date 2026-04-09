package ar.edu.itba.paw.webapp.form;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.models.Category;

public class ProductForm {

    private String sellerEmail;
    private String title;
    private String artist;
    private String recordLabel;
    private String catalogNumber;
    private String editionCountry;
    private List<Long> categoryIds;
    private String description;
    private BigDecimal sleeveCondition;
    private BigDecimal recordCondition;
    private String neighborhood;
    private String province;
    private LocalDate published;
    private BigDecimal price;

    public String getSellerEmail() {
        return sellerEmail;
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

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public String getDescription() {
        return description;
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

    public LocalDate getPublished() {
        return published;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setSellerEmail() {
        this.sellerEmail = sellerEmail;
    }

    public void setTitle() {
        this.title = title;
    }

    public void setArtist() {
        this.artist = artist;
    }

    public void setRecordLabel() {
        this.recordLabel = recordLabel;
    }

    public void setCatalogNumber() {
        this.catalogNumber = catalogNumber;
    }

    public void setEditionCountry() {
        this.editionCountry = editionCountry;
    }

    public void setCategoryIds() {
        this.categoryIds = categoryIds;
    }

    public void setDescription() {
        this.description = description;
    }

    public void setSleeveCondition() {
        this.sleeveCondition = sleeveCondition;
    }

    public void setRecordCondition() {
        this.recordCondition = recordCondition;
    }

    public void setNeighborhood() {
        this.neighborhood = neighborhood;
    }

    public void setProvince() {
        this.province = province;
    }

    public void setPublished() {
        this.published = published;
    }

    public void setPrice() {
        this.price = price;
    }
}
