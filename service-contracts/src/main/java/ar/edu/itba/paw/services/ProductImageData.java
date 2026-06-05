package ar.edu.itba.paw.services;

import java.util.Arrays;

public final class ProductImageData {

    private final byte[] data;
    private final String contentType;

    public ProductImageData(final byte[] data, final String contentType) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Image data cannot be empty");
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Image content type cannot be empty");
        }
        this.data = Arrays.copyOf(data, data.length);
        this.contentType = contentType.trim();
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    public String getContentType() {
        return contentType;
    }
}
