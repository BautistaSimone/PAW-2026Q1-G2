package ar.edu.itba.paw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

@Service
public class PurchaseDisplayServiceImpl implements PurchaseDisplayService {

    private final PurchaseService purchaseService;
    private final ReviewService reviewService;

    @Autowired
    public PurchaseDisplayServiceImpl(
            final PurchaseService purchaseService,
            final ReviewService reviewService) {
        this.purchaseService = purchaseService;
        this.reviewService = reviewService;
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseDisplay getPurchaseDisplay(final Long purchaseId, final Long userId) {
        final PurchaseService.PurchaseDetails details = purchaseService.getPurchaseDetailsForUser(
                purchaseId, userId);

        final Purchase purchase = details.getPurchase();
        final boolean hasReview = details.isBuyerView()
                && purchase.getStatus() == PurchaseStatus.DELIVERED
                && reviewService.findByPurchaseId(purchaseId).isPresent();

        return new PurchaseDisplay(
                purchase,
                details.getProduct(),
                details.getBuyer(),
                details.getSeller(),
                details.isBuyerView(),
                details.isSellerView(),
                details.getRemainingReservationSeconds(),
                details.hasPaymentProof(),
                details.getPaymentProofFileName(),
                hasReview);
    }
}