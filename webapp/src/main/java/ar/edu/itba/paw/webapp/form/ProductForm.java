package ar.edu.itba.paw.webapp.form;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.PositiveOrZero;

import org.springframework.web.multipart.MultipartFile;

public class ProductForm {

    @NotBlank
    @Size(min = 1, max = 100)
    private String title;

    @NotBlank
    @Size(min = 1, max = 100)
    private String artist;

    @NotBlank
    @Size(min = 1, max = 100)
    private String recordLabel;

    @NotBlank
    @Size(min = 1, max = 100)
    private String catalogNumber;

    @NotBlank
    @Size(min = 1, max = 100)
    private String editionCountry;

    @NotEmpty
    private List<@PositiveOrZero Long> categories;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("10.0")
    private BigDecimal sleeveCondition;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("10.0")
    private BigDecimal recordCondition;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotBlank
    @Size(max = 1000)
    private String description;

    private MultipartFile[] images;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getRecordLabel() {
        return recordLabel;
    }

    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getEditionCountry() {
        return editionCountry;
    }

    public void setEditionCountry(String editionCountry) {
        this.editionCountry = editionCountry;
    }

    public List<Long> getCategories() {
        return categories;
    }

    public void setCategories(List<Long> categories) {
        this.categories = categories;
    }

    public BigDecimal getSleeveCondition() {
        return sleeveCondition;
    }

    public void setSleeveCondition(BigDecimal sleeveCondition) {
        this.sleeveCondition = sleeveCondition;
    }

    public BigDecimal getRecordCondition() {
        return recordCondition;
    }

    public void setRecordCondition(BigDecimal recordCondition) {
        this.recordCondition = recordCondition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile[] getImages() {
        return images;
    }

    public void setImages(MultipartFile[] images) {
        this.images = images;
    }
}
