package ar.edu.itba.paw.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NewVinylDigestSchedulerTest {

    @InjectMocks
    private NewVinylDigestScheduler newVinylDigestScheduler;

    @Mock
    private PendingNotificationService pendingNotificationService;

    @Test
    public void testSendDailyDigestSuccess() {
        // Act
        newVinylDigestScheduler.sendDailyDigest();

        // Assert
        Mockito.verify(pendingNotificationService, Mockito.times(1))
                .processAndSendDigestEmails();
    }

    @Test
    public void testSendDailyDigestError() {
        // Arrange
        Mockito.doThrow(new RuntimeException("Database down"))
                .when(pendingNotificationService).processAndSendDigestEmails();

        // Act
        newVinylDigestScheduler.sendDailyDigest();

        // Assert — Should log but not crash
        Mockito.verify(pendingNotificationService, Mockito.times(1))
                .processAndSendDigestEmails();
    }
}
