package ar.edu.itba.paw.services;

import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Notification;
import ar.edu.itba.paw.models.NotificationType;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.persistence.NotificationDao;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationDao notificationDao;

    @Test
    public void testNotifyFollowSuccess() {
        // Act
        notificationService.notifyFollow(1L, 2L);

        // Assert
        Mockito.verify(notificationDao, Mockito.times(1))
                .create(NotificationType.FOLLOW, 1L, 2L, null, null, null);
    }

    @Test
    public void testNotifyFollowSelf() {
        // Act
        notificationService.notifyFollow(1L, 1L);

        // Assert
        Mockito.verifyNoInteractions(notificationDao);
    }

    @Test
    public void testNotifyFollowNulls() {
        // Assert & Act
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyFollow(null, 2L);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyFollow(1L, null);
        });
        Mockito.verifyNoInteractions(notificationDao);
    }

    @Test
    public void testNotifyNewProductSuccess() {
        // Act
        notificationService.notifyNewProduct(1L, 100L);

        // Assert
        Mockito.verify(notificationDao, Mockito.times(1))
                .createForAllFollowers(NotificationType.NEW_PRODUCT, 1L, 100L);
    }

    @Test
    public void testNotifyNewProductNulls() {
        // Assert & Act
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyNewProduct(null, 100L);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyNewProduct(1L, null);
        });
        Mockito.verifyNoInteractions(notificationDao);
    }

    @Test
    public void testNotifyPurchaseStatusSuccess() {
        // Act
        notificationService.notifyPurchaseStatus(1L, 2L, 10L, 100L, PurchaseStatus.PENDING);

        // Assert
        Mockito.verify(notificationDao, Mockito.times(1))
                .create(NotificationType.PURCHASE_STATUS, 1L, 2L, 100L, 10L, PurchaseStatus.PENDING.name());
    }

    @Test
    public void testNotifyPurchaseStatusCancelledWithoutActor() {
        // Act
        notificationService.notifyPurchaseStatus(1L, null, 10L, 100L, PurchaseStatus.CANCELLED);

        // Assert
        Mockito.verify(notificationDao, Mockito.times(1))
                .create(NotificationType.PURCHASE_STATUS, 1L, null, 100L, 10L, PurchaseStatus.CANCELLED.name());
    }

    @Test
    public void testNotifyPurchaseStatusNonCancelledWithoutActorThrows() {
        // Assert & Act
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyPurchaseStatus(1L, null, 10L, 100L, PurchaseStatus.PENDING);
        });
        Mockito.verifyNoInteractions(notificationDao);
    }

    @Test
    public void testNotifyPurchaseStatusNulls() {
        // Assert & Act
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyPurchaseStatus(null, 2L, 10L, 100L, PurchaseStatus.PENDING);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyPurchaseStatus(1L, 2L, null, 100L, PurchaseStatus.PENDING);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyPurchaseStatus(1L, 2L, 10L, 100L, null);
        });
        Mockito.verifyNoInteractions(notificationDao);
    }

    @Test
    public void testNotifyReviewReceivedSuccess() {
        // Act
        notificationService.notifyReviewReceived(1L, 2L, 10L, 100L);

        // Assert
        Mockito.verify(notificationDao, Mockito.times(1))
                .create(NotificationType.REVIEW_RECEIVED, 1L, 2L, 100L, 10L, null);
    }

    @Test
    public void testNotifyReviewReceivedNulls() {
        // Assert & Act
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyReviewReceived(null, 2L, 10L, 100L);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyReviewReceived(1L, null, 10L, 100L);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            notificationService.notifyReviewReceived(1L, 2L, null, 100L);
        });
        Mockito.verifyNoInteractions(notificationDao);
    }

    @Test
    public void testListForUser() {
        // Arrange
        PaginatedResult<Notification> expected = new PaginatedResult<>(Collections.emptyList(), 1, 10, 0);
        Mockito.when(notificationDao.findByRecipient(1L, NotificationType.FOLLOW, 1, 10)).thenReturn(expected);

        // Act
        PaginatedResult<Notification> result = notificationService.listForUser(1L, NotificationType.FOLLOW, 1, 10);

        // Assert
        Assertions.assertSame(expected, result);
    }

    @Test
    public void testCountUnread() {
        // Arrange
        Mockito.when(notificationDao.countUnread(1L)).thenReturn(5L);

        // Act
        long count = notificationService.countUnread(1L);

        // Assert
        Assertions.assertEquals(5, count);
    }

    @Test
    public void testMarkRead() {
        // Act
        notificationService.markRead(1L, 10L);

        // Assert
        Mockito.verify(notificationDao, Mockito.times(1))
                .markRead(1L, Collections.singletonList(10L));
    }

    @Test
    public void testMarkAllRead() {
        // Act
        notificationService.markAllRead(1L);

        // Assert
        Mockito.verify(notificationDao, Mockito.times(1))
                .markAllRead(1L);
    }
}
