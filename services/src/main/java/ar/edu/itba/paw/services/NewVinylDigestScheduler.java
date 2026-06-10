package ar.edu.itba.paw.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewVinylDigestScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewVinylDigestScheduler.class);

    private final PendingNotificationService pendingNotificationService;

    @Autowired
    public NewVinylDigestScheduler(final PendingNotificationService pendingNotificationService) {
        this.pendingNotificationService = pendingNotificationService;
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void sendDailyDigest() {
        LOGGER.info("Starting daily new-vinyl digest email job");
        try {
            pendingNotificationService.processAndSendDigestEmails();
        } catch (Exception e) {
            LOGGER.error("Error in daily digest email job", e);
        }
        LOGGER.info("Finished daily new-vinyl digest email job");
    }
}
