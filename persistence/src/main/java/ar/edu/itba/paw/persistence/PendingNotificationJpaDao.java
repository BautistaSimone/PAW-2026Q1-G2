package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.PendingNotification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class PendingNotificationJpaDao implements PendingNotificationDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void create(Long followerUserId, Long productId) {
        final PendingNotification pn = new PendingNotification(followerUserId, productId);
        em.persist(pn);
    }

    @Override
    public void createForAllFollowersOf(Long sellerUserId, Long productId) {
        em.createNativeQuery(
            "INSERT INTO pending_notifications (follower_user_id, product_id) " +
            "SELECT uf.follower_id, :productId " +
            "FROM user_follows uf " +
            "WHERE uf.followed_id = :sellerId"
        )
        .setParameter("productId", productId)
        .setParameter("sellerId", sellerUserId)
        .executeUpdate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PendingNotification> findAll() {
        return em.createQuery(
            "FROM PendingNotification pn ORDER BY pn.followerUserId",
            PendingNotification.class
        ).getResultList();
    }

    @Override
    public void deleteByIds(List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        em.createQuery(
            "DELETE FROM PendingNotification pn WHERE pn.notificationId IN :ids"
        )
        .setParameter("ids", notificationIds)
        .executeUpdate();
    }
}
