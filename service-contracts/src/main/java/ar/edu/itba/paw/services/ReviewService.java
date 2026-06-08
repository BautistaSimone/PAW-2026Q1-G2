package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;

public interface ReviewService {
    ReviewEligibility getReviewEligibility(long purchaseId, long buyerId);

    Review create(long purchaseId, long buyerId, int score, String text);

    Optional<Review> findByPurchaseId(long purchaseId);

    Set<Long> findReviewedPurchaseIds(Set<Long> purchaseIds);

    PaginatedResult<Review> findBySellerId(long sellerId, int page, int pageSize);

    SellerRatingSummary summaryForSeller(long sellerId);

    Map<Long, SellerRatingSummary> sellerRatingByUserId(final Set<Long> distinctSellerIds);

    /** Maps each purchase id to whether it already has a review. */
    Map<Long, Boolean> reviewStatusByPurchaseId(final List<Purchase> purchases);

    /** Seller rating summaries keyed by user id, for the distinct sellers of the given products. */
    Map<Long, SellerRatingSummary> sellerRatingByProducts(final List<Product> products);
}
