package ar.edu.itba.paw.services;

import java.util.Arrays;

public class PurchasePaymentProof {

    private final byte[] data;
    private final String contentType;
    private final String fileName;

    public PurchasePaymentProof(final byte[] data, final String contentType, final String fileName) {
        this.data = data == null ? null : Arrays.copyOf(data, data.length);
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data == null ? null : Arrays.copyOf(data, data.length);
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }
}
