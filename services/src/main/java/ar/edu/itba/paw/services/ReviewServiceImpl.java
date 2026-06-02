package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.persistence.ReviewDao;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final NotificationService notificationService;

    @Autowired
    public ReviewServiceImpl(
        final ReviewDao reviewDao,
        final PurchaseService purchaseService,
        final ProductService productService,
        final NotificationService notificationService
    ) {
        this.reviewDao = reviewDao;
        this.purchaseService = purchaseService;
        this.productService = productService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Review create(long purchaseId, long buyerId, int score, String text) {
        final Purchase purchase = purchaseService.findById(purchaseId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (!purchase.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("Only the buyer can leave a review");
        }

        if (purchase.getStatus() != PurchaseStatus.DELIVERED) {
            throw new IllegalStateException("Can only review after delivery");
        }

        if (reviewDao.findByPurchaseId(purchaseId).isPresent()) {
            throw new IllegalStateException("A review already exists for this purchase");
        }

        final Product product = productService.findById(purchase.getProductId())
            .orElseThrow(() -> new IllegalStateException("Product not found"));

        final long sellerId = product.getUserId();

        final Review review = reviewDao.create(purchaseId, sellerId, buyerId, score, text);
        notificationService.notifyReviewReceived(sellerId, buyerId, purchaseId, product.getId());
        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findByPurchaseId(long purchaseId) {
        return reviewDao.findByPurchaseId(purchaseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findReviewedPurchaseIds(final Set<Long> purchaseIds) {
        if (purchaseIds == null || purchaseIds.isEmpty()) {
            return Collections.emptySet();
        }
        return reviewDao.findReviewedPurchaseIds(purchaseIds);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Review> findBySellerId(long sellerId, int page, int pageSize) {
        return reviewDao.findBySellerId(sellerId, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerRatingSummary summaryForSeller(long sellerId) {
        return reviewDao.summaryForSeller(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, SellerRatingSummary> sellerRatingByUserId(final Set<Long> distinctSellerIds) {
        if (distinctSellerIds == null || distinctSellerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewDao.sellerRatingByUserId(distinctSellerIds);
    }
}
