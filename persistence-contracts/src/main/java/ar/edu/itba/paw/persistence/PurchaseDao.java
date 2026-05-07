package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

public interface PurchaseDao {
    Purchase createPurchase(Long productId, Long buyerId, Long sellerId, PurchaseStatus status, String buyerToken, String sellerToken);
    Optional<Purchase> findById(Long purchaseId);
    void updateStatus(Long purchaseId, PurchaseStatus status);
    PaginatedResult<Purchase> findByBuyerId(Long buyerId, List<PurchaseStatus> statuses, int page, int pageSize);
    PaginatedResult<Purchase> findBySellerId(Long sellerId, List<PurchaseStatus> statuses, int page, int pageSize);
}
