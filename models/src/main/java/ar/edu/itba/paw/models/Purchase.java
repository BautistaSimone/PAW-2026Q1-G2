package ar.edu.itba.paw.models;

import java.time.LocalDate;

public class Purchase {
    private final Long purchaseId;
    private final Long productId;
    private final Long buyerId;
    /** Seller at the time of purchase (matches {@code purchases.seller_user_id}). */
    private final Long sellerId;
    private final LocalDate date;
    private final PurchaseStatus status;
    private final String buyerToken;
    private final String sellerToken;

    public Purchase(
        final Long purchaseId,
        final Long productId,
        final Long buyerId,
        final Long sellerId,
        final LocalDate date,
        final PurchaseStatus status,
        final String buyerToken,
        final String sellerToken
    ) {
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
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

    public Long getSellerId() {
        return sellerId;
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
