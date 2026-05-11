package ar.edu.itba.paw.models;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Lob;

@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "images_imageid_seq")
    @SequenceGenerator(sequenceName = "images_imageid_seq", name = "images_imageid_eq", allocationSize = 1)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Lob
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] data;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    Image() {
        // Just for Hibernate, we love you!
    }

    public Image(Long productId, byte[] data, String contentType) {
        this.productId = productId;
        this.data = data;
        this.contentType = contentType;
    }

    public Image(Long imageId, Long productId, byte[] data, String contentType) {
        this.imageId = imageId;
        this.productId = productId;
        this.data = data;
        this.contentType = contentType;
    }

    public Long getImageId() {
        return imageId;
    }

    public Long getProductId() {
        return productId;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return 
            "Image [product_id=" + productId 
            + ", contentType=" + contentType
            + "]";
    }
}
