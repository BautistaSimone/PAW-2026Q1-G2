package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.PendingNotification;
import java.util.List;

public interface PendingNotificationDao {

    void create(Long followerUserId, Long productId);

    void createForAllFollowersOf(Long sellerUserId, Long productId);

    List<PendingNotification> findAll();

    void deleteByIds(List<Long> notificationIds);
}
