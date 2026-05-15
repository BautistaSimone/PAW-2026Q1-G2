package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ReviewJpaDaoTest {

    @Autowired
    private ReviewJpaDao reviewDao;

    @Autowired
    private UserJpaDao userDao;

    @Autowired
    private ProductJpaDao productDao;

    @Autowired
    private PurchaseJpaDao purchaseDao;

    @PersistenceContext
    private EntityManager em;

    private long sellerId;
    private long buyerId;
    private long purchaseId;

    @BeforeEach
    public void setUp() {
        final User seller = userDao.createUser("review-seller@test.com", "pass", "Seller",
            false, true, null, null, null, null, null, null, null, null);
        final User buyer = userDao.createUser("review-buyer@test.com", "pass", "Buyer",
            false, true, null, null, null, null, null, null, null, null);

        sellerId = seller.getId();
        buyerId = buyer.getId();

        final Product product = productDao.createProduct(
            sellerId, "Test Album", "Test Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "desc", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000)
        );

        final Purchase purchase = purchaseDao.createPurchase(
            product.getId(), buyerId, sellerId, PurchaseStatus.DELIVERED, "token1", "token2"
        );
        purchaseId = purchase.getPurchaseId();
        em.flush();
    }

    @Test
    public void testCreateReview() {
        // Arrange

        // Act
        final Review review = reviewDao.create(purchaseId, sellerId, buyerId, 4, "Great seller!");
        em.flush();

        // Assert
        Assertions.assertNotNull(review);
        Assertions.assertEquals(4, review.getScore());

        final long count = em.createQuery("SELECT COUNT(r) FROM Review r", Long.class).getSingleResult();
        Assertions.assertEquals(1, count);
    }

    @Test
    public void testFindByPurchaseId() {
        // Arrange
        reviewDao.create(purchaseId, sellerId, buyerId, 5, "Excellent");
        em.flush();
        em.clear();

        // Act
        final Optional<Review> result = reviewDao.findByPurchaseId(purchaseId);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(5, result.get().getScore());
        Assertions.assertEquals("Buyer", result.get().getBuyerUsername());
    }

    @Test
    public void testFindBySellerId() {
        // Arrange
        reviewDao.create(purchaseId, sellerId, buyerId, 3, "OK");
        em.flush();
        em.clear();

        // Act
        final var reviews = reviewDao.findBySellerId(sellerId, 1, 10).getResults();

        // Assert
        Assertions.assertEquals(1, reviews.size());
        Assertions.assertEquals(3, reviews.get(0).getScore());
    }

    @Test
    public void testSummaryForSeller() {
        // Arrange
        reviewDao.create(purchaseId, sellerId, buyerId, 4, "Good");
        em.flush();

        final SellerRatingSummary summary = reviewDao.summaryForSeller(sellerId);

        Assertions.assertEquals(1, summary.getCount());
        Assertions.assertEquals(4.0, summary.getAvgScore(), 0.01);
    }

    @Test
    public void testSummaryForSellerWithNoReviews() {
        final SellerRatingSummary summary = reviewDao.summaryForSeller(sellerId);

        Assertions.assertEquals(0, summary.getCount());
        Assertions.assertEquals(0.0, summary.getAvgScore(), 0.01);
    }
}
