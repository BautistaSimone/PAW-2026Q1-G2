package ar.edu.itba.paw.models;

import java.time.LocalDate;

public class Purchase {
    private final Long purchaseId;
    private final Long productId;
    private final Long buyerId;
    private final LocalDate date;
    private final PurchaseStatus status;
    private final String buyerToken;
    private final String sellerToken;

    public Purchase(
        final Long purchaseId, 
        final Long productId, 
        final Long buyerId, 
        final LocalDate date,
        final PurchaseStatus status, 
        final String buyerToken, 
        final String sellerToken
    ) {
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.date = date;
        this.status = status;
        this.buyerToken = buyerToken;
        this.sellerToken = sellerToken;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public String getBuyerToken() {
        return buyerToken;
    }

    public String getSellerToken() {
        return sellerToken;
    }
}
