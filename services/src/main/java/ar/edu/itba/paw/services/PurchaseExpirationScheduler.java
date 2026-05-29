package ar.edu.itba.paw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PurchaseExpirationScheduler {

    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseExpirationScheduler(final PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Scheduled(fixedDelay = 60000)
    public void cancelExpiredReservations() {
        purchaseService.cancelExpiredPurchases();
    }
}
