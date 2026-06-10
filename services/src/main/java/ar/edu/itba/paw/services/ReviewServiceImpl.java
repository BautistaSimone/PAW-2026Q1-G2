package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.ReviewDao;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public ReviewServiceImpl(
            final ReviewDao reviewDao,
            final PurchaseService purchaseService,
            final ProductService productService,
            final UserService userService,
            final NotificationService notificationService) {
        this.reviewDao = reviewDao;
        this.purchaseService = purchaseService;
        this.productService = productService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ReviewEligibility getReviewEligibility(final long purchaseId, final long buyerId) {
        final Purchase purchase = purchaseService.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (purchase.getBuyerId() != buyerId) {
            return ReviewEligibility.unavailable(ReviewEligibility.Status.NOT_BUYER);
        }
        if (purchase.getStatus() != PurchaseStatus.DELIVERED) {
            return ReviewEligibility.unavailable(ReviewEligibility.Status.NOT_DELIVERED);
        }
        if (reviewDao.findByPurchaseId(purchaseId).isPresent()) {
            return ReviewEligibility.unavailable(ReviewEligibility.Status.ALREADY_REVIEWED);
        }

        final Product product = productService.findById(purchase.getProductId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));
        final User seller = userService.findById(purchase.getSellerId())
                .orElseThrow(() -> new IllegalStateException("Seller not found"));

        return ReviewEligibility.available(new ReviewContext(purchase, product, seller));
    }

    @Override
    @Transactional
    public Review create(long purchaseId, long buyerId, int score, String text) {
        final ReviewEligibility eligibility = getReviewEligibility(purchaseId, buyerId);
        if (eligibility.getStatus() == ReviewEligibility.Status.NOT_BUYER) {
            throw new IllegalArgumentException("Only the buyer can leave a review");
        }
        if (eligibility.getStatus() == ReviewEligibility.Status.NOT_DELIVERED) {
            throw new IllegalStateException("Can only review after delivery");
        }
        if (eligibility.getStatus() == ReviewEligibility.Status.ALREADY_REVIEWED) {
            throw new IllegalStateException("A review already exists for this purchase");
        }

        final ReviewContext context = eligibility.getContext();
        final Product product = context.getProduct();
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

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> reviewStatusByPurchaseId(final List<Purchase> purchases) {
        final Map<Long, Boolean> purchaseHasReview = new HashMap<>();
        if (purchases == null || purchases.isEmpty()) {
            return purchaseHasReview;
        }

        final Set<Long> purchaseIds = new HashSet<>();
        for (Purchase purchase : purchases) {
            purchaseIds.add(purchase.getPurchaseId());
        }
        final Set<Long> reviewedPurchaseIds = findReviewedPurchaseIds(purchaseIds);
        for (Purchase purchase : purchases) {
            purchaseHasReview.put(purchase.getPurchaseId(), reviewedPurchaseIds.contains(purchase.getPurchaseId()));
        }
        return purchaseHasReview;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, SellerRatingSummary> sellerRatingByProducts(final List<Product> products) {
        final Set<Long> distinctSellerIds = new HashSet<>();
        if (products != null) {
            for (Product product : products) {
                distinctSellerIds.add(product.getUserId());
            }
        }
        return sellerRatingByUserId(distinctSellerIds);
    }
}
