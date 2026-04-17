package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

public class Review {

    private final Long reviewId;
    private final Long purchaseId;
    private final Long sellerId;
    private final Long buyerId;
    private final int score;
    private final String text;
    private final LocalDateTime createdAt;
    private final String buyerUsername;

    public Review(
        final Long reviewId,
        final Long purchaseId,
        final Long sellerId,
        final Long buyerId,
        final int score,
        final String text,
        final LocalDateTime createdAt,
        final String buyerUsername
    ) {
        this.reviewId = reviewId;
        this.purchaseId = purchaseId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.score = score;
        this.text = text;
        this.createdAt = createdAt;
        this.buyerUsername = buyerUsername;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public int getScore() {
        return score;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }
}
