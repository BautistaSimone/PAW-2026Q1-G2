package ar.edu.itba.paw.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;

public interface PurchaseService {
    Purchase createPurchase(Long productId, Long userId);

    boolean canCreatePurchases(Long userId);

    Optional<Purchase> findById(Long purchaseId);

    PurchaseDetails getPurchaseDetailsForUser(Long purchaseId, Long userId);

    Optional<PurchasePaymentProof> findPaymentProofForSeller(Long purchaseId, Long userId);

    Purchase updateStatus(
        Long purchaseId,
        Long userId,
        PurchaseStatus newStatus,
        byte[] paymentProof,
        String paymentProofContentType,
        String paymentProofFileName
    );

    int cancelExpiredPurchases();

    PaginatedResult<Purchase> findByBuyerId(Long buyerId, List<PurchaseStatus> statuses, int page, int pageSize);

    PaginatedResult<Purchase> findBySellerId(Long sellerId, List<PurchaseStatus> statuses, int page, int pageSize);

    final class PurchaseDetails {

        private final Purchase purchase;
        private final Product product;
        private final User buyer;
        private final User seller;
        private final boolean buyerView;
        private final boolean sellerView;

        public PurchaseDetails(
                final Purchase purchase,
                final Product product,
                final User buyer,
                final User seller,
                final boolean buyerView,
                final boolean sellerView) {
            this.purchase = purchase;
            this.product = product;
            this.buyer = buyer;
            this.seller = seller;
            this.buyerView = buyerView;
            this.sellerView = sellerView;
        }

        public Purchase getPurchase() {
            return purchase;
        }

        public Product getProduct() {
            return product;
        }

        public User getBuyer() {
            return buyer;
        }

        public User getSeller() {
            return seller;
        }

        public boolean isBuyerView() {
            return buyerView;
        }

        public boolean isSellerView() {
            return sellerView;
        }

        public boolean hasPaymentProof() {
            return purchase.getPaymentProof() != null
                    && purchase.getPaymentProof().length > 0
                    && purchase.getPaymentProofContentType() != null;
        }

        public String getPaymentProofFileName() {
            return purchase.getPaymentProofFileName();
        }

        /**
         * Seconds left in the reservation window for a pending purchase (floored at 0),
         * or {@code null} when the purchase is not pending or has no reservation deadline.
         */
        public Long getRemainingReservationSeconds() {
            if (purchase.getStatus() != PurchaseStatus.PENDING || purchase.getReservedUntil() == null) {
                return null;
            }
            final long remaining = Duration.between(LocalDateTime.now(), purchase.getReservedUntil()).getSeconds();
            return remaining < 0 ? 0L : remaining;
        }
    }
}
