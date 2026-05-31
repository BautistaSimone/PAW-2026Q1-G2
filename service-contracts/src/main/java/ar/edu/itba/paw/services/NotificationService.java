package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.PurchaseStatus;

public interface NotificationService {

    void notifyFollow(Long followedUserId, Long followerUserId);

    void notifyNewProduct(Long sellerUserId, Long productId);

    void notifyPurchaseStatus(Long recipientUserId, Long actorUserId, Long purchaseId, Long productId, PurchaseStatus status);

    void notifyReviewReceived(Long sellerUserId, Long buyerUserId, Long purchaseId, Long productId);

    PaginatedResult<Notification> listForUser(Long recipientUserId, NotificationType filter, int page, int pageSize);

    long countUnread(Long recipientUserId);

    void markRead(Long recipientUserId, Long notificationId);

    void markAllRead(Long recipientUserId);
}
