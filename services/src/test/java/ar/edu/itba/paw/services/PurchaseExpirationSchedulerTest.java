package ar.edu.itba.paw.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PurchaseExpirationSchedulerTest {

    @InjectMocks
    private PurchaseExpirationScheduler purchaseExpirationScheduler;

    @Mock
    private PurchaseService purchaseService;

    @Test
    public void testCancelExpiredReservations() {
        // Act
        purchaseExpirationScheduler.cancelExpiredReservations();

        // Assert
        Mockito.verify(purchaseService, Mockito.times(1))
                .cancelExpiredPurchases();
    }
}
