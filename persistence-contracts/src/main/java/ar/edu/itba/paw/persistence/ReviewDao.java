package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;

public interface ReviewDao {
    Review create(long purchaseId, long sellerId, long buyerId, int score, String text);
    Optional<Review> findByPurchaseId(long purchaseId);
    PaginatedResult<Review> findBySellerId(long sellerId, int page, int pageSize);
    SellerRatingSummary summaryForSeller(long sellerId);
}
