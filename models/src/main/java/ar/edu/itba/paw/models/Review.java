package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Transient;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reviews_review_id_seq")
    @SequenceGenerator(sequenceName = "reviews_review_id_seq", name = "reviews_review_id_seq", allocationSize = 1)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "purchase_id", nullable = false, unique = true)
    private long purchaseId;

    @Column(name = "seller_id", nullable = false)
    private long sellerId;

    @Column(name = "buyer_id", nullable = false)
    private long buyerId;

    @Column(nullable = false)
    private int score;

    @Column(name = "review")
    private String text;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private String buyerUsername;

    Review() {
        // Just for Hibernate, we love you!
    }

    public Review(
        final Long reviewId,
        final long purchaseId,
        final long sellerId,
        final long buyerId,
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

    public Review(
        final long purchaseId,
        final long sellerId,
        final long buyerId,
        final int score,
        final String text,
        final LocalDateTime createdAt
    ) {
        this.purchaseId = purchaseId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.score = score;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public long getPurchaseId() {
        return purchaseId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public long getBuyerId() {
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

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }
}
