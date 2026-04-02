package ar.edu.itba.paw.models;

public class Image {

    private final Long imageId;
    private final Long productId;
    private final byte[] data;

    public Image(Long imageId, Long productId, byte[] data) {
        this.imageId = imageId;
        this.productId = productId;
        this.data = data;
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

    @Override
    public String toString() {
        return 
            "Image [product_id=" + productId 
            + "]";
    }
}