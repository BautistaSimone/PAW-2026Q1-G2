package ar.edu.itba.paw.services;

import java.util.Optional;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

public interface PurchaseService {
    Purchase createPurchase(Long productId, Long userId);
    Optional<Purchase> findById(Long purchaseId);
    Purchase updateStatus(Long purchaseId, String token, PurchaseStatus newStatus);
}
