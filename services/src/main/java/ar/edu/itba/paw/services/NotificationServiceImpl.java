package ar.edu.itba.paw.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.persistence.NotificationDao;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;

    @Autowired
    public NotificationServiceImpl(final NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    @Transactional
    public void notifyFollow(final Long followedUserId, final Long followerUserId) {
        if (followedUserId == null || followerUserId == null) {
            throw new IllegalArgumentException("User ids are required");
        }
        if (followedUserId.equals(followerUserId)) {
            return;
        }
        notificationDao.create(NotificationType.FOLLOW, followedUserId, followerUserId, null, null, null);
    }

    @Override
    @Transactional
    public void notifyNewProduct(final Long sellerUserId, final Long productId) {
        if (sellerUserId == null || productId == null) {
            throw new IllegalArgumentException("Seller and product ids are required");
        }
        notificationDao.createForAllFollowers(NotificationType.NEW_PRODUCT, sellerUserId, productId);
    }

    @Override
    @Transactional
    public void notifyPurchaseStatus(
            final Long recipientUserId,
            final Long actorUserId,
            final Long purchaseId,
            final Long productId,
            final PurchaseStatus status) {
        if (recipientUserId == null || purchaseId == null || status == null) {
            throw new IllegalArgumentException("Purchase notification data is required");
        }
        if (actorUserId == null && status != PurchaseStatus.CANCELLED) {
            throw new IllegalArgumentException("Actor user is required for non-cancelled status");
        }
        notificationDao.create(
                NotificationType.PURCHASE_STATUS,
                recipientUserId,
                actorUserId,
                productId,
                purchaseId,
                status.name()
        );
    }

    @Override
    @Transactional
    public void notifyReviewReceived(
            final Long sellerUserId,
            final Long buyerUserId,
            final Long purchaseId,
            final Long productId) {
        if (sellerUserId == null || buyerUserId == null || purchaseId == null) {
            throw new IllegalArgumentException("Review notification data is required");
        }
        notificationDao.create(
                NotificationType.REVIEW_RECEIVED,
                sellerUserId,
                buyerUserId,
                productId,
                purchaseId,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Notification> listForUser(
            final Long recipientUserId,
            final NotificationType filter,
            final int page,
            final int pageSize) {
        return notificationDao.findByRecipient(recipientUserId, filter, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(final Long recipientUserId) {
        return notificationDao.countUnread(recipientUserId);
    }

    @Override
    @Transactional
    public void markRead(final Long recipientUserId, final Long notificationId) {
        notificationDao.markRead(recipientUserId, Collections.singletonList(notificationId));
    }

    @Override
    @Transactional
    public void markAllRead(final Long recipientUserId) {
        notificationDao.markAllRead(recipientUserId);
    }
}
