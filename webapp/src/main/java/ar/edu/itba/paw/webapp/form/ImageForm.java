package ar.edu.itba.paw.webapp.form;

import org.springframework.web.multipart.MultipartFile;

public class ImageForm {

    private Long productId;
    private MultipartFile image;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

}
