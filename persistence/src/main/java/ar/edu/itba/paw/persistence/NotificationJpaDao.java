package ar.edu.itba.paw.persistence;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;

@Repository
public class NotificationJpaDao implements NotificationDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Notification create(
            final NotificationType type,
            final Long recipientUserId,
            final Long actorUserId,
            final Long productId,
            final Long purchaseId,
            final String purchaseStatus) {
        final Notification notification = new Notification(
                recipientUserId,
                actorUserId,
                type,
                productId,
                purchaseId,
                purchaseStatus
        );
        em.persist(notification);
        return notification;
    }

    @Override
    public void createForAllFollowers(final NotificationType type, final Long sellerUserId, final Long productId) {
        em.createNativeQuery(
            "INSERT INTO notifications (recipient_user_id, actor_user_id, type, product_id, created_at) "
            + "SELECT uf.follower_id, :sellerId, :type, :productId, :createdAt "
            + "FROM user_follows uf "
            + "WHERE uf.followed_id = :sellerId"
        )
        .setParameter("sellerId", sellerUserId)
        .setParameter("type", type.name())
        .setParameter("productId", productId)
        .setParameter("createdAt", LocalDateTime.now())
        .executeUpdate();
    }

    @Override
    public PaginatedResult<Notification> findByRecipient(
            final Long recipientUserId,
            final NotificationType typeFilter,
            final int page,
            final int pageSize) {

        final StringBuilder whereJpql = new StringBuilder("WHERE n.recipientUserId = :recipientUserId");
        if (typeFilter != null) {
            whereJpql.append(" AND n.type = :type");
        }

        final TypedQuery<Long> countQuery = em.createQuery(
            "SELECT COUNT(n) FROM Notification n " + whereJpql, Long.class
        );
        countQuery.setParameter("recipientUserId", recipientUserId);
        if (typeFilter != null) {
            countQuery.setParameter("type", typeFilter);
        }

        final long totalCount = countQuery.getSingleResult();
        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), page, pageSize, 0);
        }

        final TypedQuery<Notification> selectQuery = em.createQuery(
            "SELECT n FROM Notification n " + whereJpql + " ORDER BY n.createdAt DESC",
            Notification.class
        );
        selectQuery.setParameter("recipientUserId", recipientUserId);
        if (typeFilter != null) {
            selectQuery.setParameter("type", typeFilter);
        }
        selectQuery.setMaxResults(pageSize);
        selectQuery.setFirstResult((page - 1) * pageSize);

        return new PaginatedResult<>(selectQuery.getResultList(), page, pageSize, totalCount);
    }

    @Override
    public long countUnread(final Long recipientUserId) {
        final Long count = em.createQuery(
            "SELECT COUNT(n) FROM Notification n WHERE n.recipientUserId = :recipientUserId AND n.readAt IS NULL",
            Long.class
        )
        .setParameter("recipientUserId", recipientUserId)
        .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public void markRead(final Long recipientUserId, final List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }

        em.createQuery(
            "UPDATE Notification n SET n.readAt = :now "
            + "WHERE n.recipientUserId = :recipientUserId "
            + "AND n.notificationId IN :ids "
            + "AND n.readAt IS NULL"
        )
        .setParameter("now", LocalDateTime.now())
        .setParameter("recipientUserId", recipientUserId)
        .setParameter("ids", notificationIds)
        .executeUpdate();
    }

    @Override
    public void markAllRead(final Long recipientUserId) {
        em.createQuery(
            "UPDATE Notification n SET n.readAt = :now "
            + "WHERE n.recipientUserId = :recipientUserId AND n.readAt IS NULL"
        )
        .setParameter("now", LocalDateTime.now())
        .setParameter("recipientUserId", recipientUserId)
        .executeUpdate();
    }
}
