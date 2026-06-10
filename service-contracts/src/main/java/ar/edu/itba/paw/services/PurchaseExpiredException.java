package ar.edu.itba.paw.services;

public class PurchaseExpiredException extends IllegalStateException {

    private final Long purchaseId;

    public PurchaseExpiredException(final String message, final Long purchaseId) {
        super(message);
        this.purchaseId = purchaseId;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }
}
