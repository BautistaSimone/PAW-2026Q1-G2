package ar.edu.itba.paw.persistence;

import java.util.Optional;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

public interface PurchaseDao {
    Purchase createPurchase(Long productId, Long buyerId, PurchaseStatus status, String buyerToken, String sellerToken);
    Optional<Purchase> findById(Long purchaseId);
    void updateStatus(Long purchaseId, PurchaseStatus status);
}
