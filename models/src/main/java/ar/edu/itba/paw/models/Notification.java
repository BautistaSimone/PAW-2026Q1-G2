package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_notification_id_seq")
    @SequenceGenerator(sequenceName = "notifications_notification_id_seq",
            name = "notifications_notification_id_seq", allocationSize = 1)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "recipient_user_id", nullable = false)
    private long recipientUserId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private NotificationType type;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(name = "purchase_status", length = 32)
    private String purchaseStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    Notification() {
        // Hibernate
    }

    public Notification(
        final long recipientUserId,
        final Long actorUserId,
        final NotificationType type,
        final Long productId,
        final Long purchaseId,
        final String purchaseStatus
    ) {
        this.recipientUserId = recipientUserId;
        this.actorUserId = actorUserId;
        this.type = type;
        this.productId = productId;
        this.purchaseId = purchaseId;
        this.purchaseStatus = purchaseStatus;
        this.createdAt = LocalDateTime.now();
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public long getRecipientUserId() {
        return recipientUserId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public NotificationType getType() {
        return type;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public String getPurchaseStatus() {
        return purchaseStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public boolean isRead() {
        return readAt != null;
    }
}
