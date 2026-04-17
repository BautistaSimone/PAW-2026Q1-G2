package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;

public interface ReviewDao {
    Review create(long purchaseId, long sellerId, long buyerId, int score, String text);
    Optional<Review> findByPurchaseId(long purchaseId);
    List<Review> findBySellerId(long sellerId);
    SellerRatingSummary summaryForSeller(long sellerId);
}
