package ar.edu.itba.paw.models;

public class Image {

    private final Long imageId;
    private final Long productId;
    private final byte[] data;
    private final String contentType;

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
