package ar.edu.itba.paw.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_notifications")
public class PendingNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pending_notifications_notificationid_seq")
    @SequenceGenerator(sequenceName = "pending_notifications_notification_id_seq",
                       name = "pending_notifications_notificationid_seq", allocationSize = 1)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "follower_user_id", nullable = false)
    private Long followerUserId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    PendingNotification() {
        // Hibernate
    }

    public PendingNotification(Long followerUserId, Long productId) {
        this.followerUserId = followerUserId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getNotificationId() { return notificationId; }
    public Long getFollowerUserId() { return followerUserId; }
    public Long getProductId() { return productId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
