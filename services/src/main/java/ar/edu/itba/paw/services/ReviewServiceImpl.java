package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    public ReviewServiceImpl(
        final ReviewDao reviewDao,
        final PurchaseService purchaseService,
        final ProductService productService
    ) {
        this.reviewDao = reviewDao;
        this.purchaseService = purchaseService;
        this.productService = productService;
    }

    @Override
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

        return reviewDao.create(purchaseId, sellerId, buyerId, score, text);
    }

    @Override
    public Optional<Review> findByPurchaseId(long purchaseId) {
        return reviewDao.findByPurchaseId(purchaseId);
    }

    @Override
    public List<Review> findBySellerId(long sellerId) {
        return reviewDao.findBySellerId(sellerId);
    }

    @Override
    public SellerRatingSummary summaryForSeller(long sellerId) {
        return reviewDao.summaryForSeller(sellerId);
    }
}
