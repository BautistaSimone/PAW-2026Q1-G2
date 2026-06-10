package ar.edu.itba.paw.services;

public class PurchaseCreationException extends RuntimeException {

    private final Reason reason;
    private final Long productId;

    public PurchaseCreationException(final Reason reason, final Long productId) {
        super(reason.message);
        this.reason = reason;
        this.productId = productId;
    }

    public Reason getReason() {
        return reason;
    }

    public Long getProductId() {
        return productId;
    }

    public enum Reason {
        MISSING_BUYER_DATA("Buyer must complete shipping data"),
        PRODUCT_NOT_FOUND("Product not found or unavailable"),
        OWN_PRODUCT("User cannot purchase their own product"),
        OUT_OF_STOCK("Product is no longer in stock");

        final String message;

        Reason(final String message) {
            this.message = message;
        }
    }
}