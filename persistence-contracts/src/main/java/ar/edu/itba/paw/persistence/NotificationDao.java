package ar.edu.itba.paw.persistence;

import java.util.List;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;

public interface NotificationDao {

    Notification create(
            NotificationType type,
            Long recipientUserId,
            Long actorUserId,
            Long productId,
            Long purchaseId,
            String purchaseStatus);

    void createForAllFollowers(NotificationType type, Long sellerUserId, Long productId);

    PaginatedResult<Notification> findByRecipient(
            Long recipientUserId,
            NotificationType typeFilter,
            int page,
            int pageSize);

    long countUnread(Long recipientUserId);

    void markRead(Long recipientUserId, List<Long> notificationIds);

    void markAllRead(Long recipientUserId);
}
