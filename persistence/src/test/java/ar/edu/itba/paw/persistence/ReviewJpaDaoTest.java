package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @PersistenceContext
    private EntityManager em;

    private long sellerId;
    private long buyerId;
    private long purchaseId;

    private User insertUser(final String email, final String username) {
        final User user = new User(
                email,
                "pass",
                username,
                false,
                true,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        em.persist(user);
        em.flush();
        return user;
    }

    private Product insertProduct(final Long sellerId, final String title) {
        final Product product = new Product(
                sellerId,
                title,
                "Test Artist",
                "Label",
                "CAT",
                "Argentina",
                Collections.emptyList(),
                "desc",
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(9),
                LocalDate.now(),
                BigDecimal.valueOf(1000),
                1);
        em.persist(product);
        em.flush();
        return product;
    }

    private Purchase insertPurchase(
            final Long productId,
            final Long buyerId,
            final Long sellerId,
            final PurchaseStatus status,
            final String buyerToken,
            final String sellerToken) {
        final Purchase purchase = new Purchase(
                productId,
                buyerId,
                sellerId,
                LocalDate.now(),
                status,
                buyerToken,
                sellerToken);
        em.persist(purchase);
        em.flush();
        return purchase;
    }

    private Review insertReview(
            final Long purchaseId,
            final Long sellerId,
            final Long buyerId,
            final int score,
            final String text) {
        final Review review = new Review(
                purchaseId,
                sellerId,
                buyerId,
                score,
                text,
                LocalDateTime.now());
        em.persist(review);
        em.flush();
        return review;
    }

    @BeforeEach
    public void setUp() {
        final User seller = insertUser("review-seller@test.com", "Seller");
        final User buyer = insertUser("review-buyer@test.com", "Buyer");

        sellerId = seller.getId();
        buyerId = buyer.getId();

        final Product product = insertProduct(sellerId, "Test Album");

        final Purchase purchase = insertPurchase(
                product.getId(), buyerId, sellerId, PurchaseStatus.DELIVERED, "token1", "token2");
        purchaseId = purchase.getPurchaseId();
        em.flush();
        em.clear();
    }

    @Test
    public void testCreateReview() {
        // Arrange

        // Act
        final Review review = reviewDao.create(purchaseId, sellerId, buyerId, 4, "Great seller!");
        em.flush();
        em.clear();

        // Assert
        Assertions.assertNotNull(review);
        Assertions.assertEquals(4, review.getScore());

        final long count = em.createQuery("SELECT COUNT(r) FROM Review r", Long.class).getSingleResult();
        Assertions.assertEquals(1, count);
    }

    @Test
    public void testFindByPurchaseId() {
        // Arrange
        insertReview(purchaseId, sellerId, buyerId, 5, "Excellent");
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
        insertReview(purchaseId, sellerId, buyerId, 3, "OK");
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
        insertReview(purchaseId, sellerId, buyerId, 4, "Good");
        em.flush();

        // Act
        final SellerRatingSummary summary = reviewDao.summaryForSeller(sellerId);

        // Assert
        Assertions.assertEquals(1, summary.getCount());
        Assertions.assertEquals(4.0, summary.getAvgScore(), 0.01);
    }

    @Test
    public void testSummaryForSellerWithNoReviews() {

        // Arrange

        // Act
        final SellerRatingSummary summary = reviewDao.summaryForSeller(sellerId);

        // Assert
        Assertions.assertEquals(0, summary.getCount());
        Assertions.assertEquals(0.0, summary.getAvgScore(), 0.01);
    }
}
