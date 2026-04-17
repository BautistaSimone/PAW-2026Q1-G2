package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ReviewJdbcDaoTest {

    @Autowired
    private ReviewJdbcDao reviewDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private long sellerId;
    private long buyerId;
    private long productId;
    private long purchaseId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);

        sellerId = jdbcTemplate.queryForObject(
            "INSERT INTO users (email, password, username, mod) VALUES ('seller@test.com', 'pass', 'Seller', false) CALL IDENTITY()",
            Long.class
        );
        buyerId = jdbcTemplate.queryForObject(
            "INSERT INTO users (email, password, username, mod) VALUES ('buyer@test.com', 'pass', 'Buyer', false) CALL IDENTITY()",
            Long.class
        );
        productId = jdbcTemplate.queryForObject(
            "INSERT INTO products (user_id, title, artist, description, sleeve_condition, record_condition, neighborhood, province, published, price) " +
            "VALUES (" + sellerId + ", 'Test Album', 'Test Artist', 'desc', 8, 9, 'Palermo', 'CABA', CURRENT_DATE, 1000) CALL IDENTITY()",
            Long.class
        );
        purchaseId = jdbcTemplate.queryForObject(
            "INSERT INTO purchases (product_id, buyer_user_id, seller_user_id, date, payment_method, confirmed) " +
            "VALUES (" + productId + ", " + buyerId + ", " + sellerId + ", CURRENT_DATE, 'DELIVERED|token1|token2', true) CALL IDENTITY()",
            Long.class
        );
    }

    @Test
    public void testCreateReview() {
        final Review review = reviewDao.create(purchaseId, sellerId, buyerId, 4, "Great seller!");

        Assertions.assertNotNull(review);
        Assertions.assertEquals(4, review.getScore());
        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reviews"));
    }

    @Test
    public void testFindByPurchaseId() {
        reviewDao.create(purchaseId, sellerId, buyerId, 5, "Excellent");

        var result = reviewDao.findByPurchaseId(purchaseId);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(5, result.get().getScore());
        Assertions.assertEquals("Buyer", result.get().getBuyerUsername());
    }

    @Test
    public void testFindBySellerId() {
        reviewDao.create(purchaseId, sellerId, buyerId, 3, "OK");

        var reviews = reviewDao.findBySellerId(sellerId);

        Assertions.assertEquals(1, reviews.size());
        Assertions.assertEquals(3, reviews.get(0).getScore());
    }

    @Test
    public void testSummaryForSeller() {
        reviewDao.create(purchaseId, sellerId, buyerId, 4, "Good");

        SellerRatingSummary summary = reviewDao.summaryForSeller(sellerId);

        Assertions.assertEquals(1, summary.getCount());
        Assertions.assertEquals(4.0, summary.getAvgScore(), 0.01);
    }

    @Test
    public void testSummaryForSellerWithNoReviews() {
        SellerRatingSummary summary = reviewDao.summaryForSeller(sellerId);

        Assertions.assertEquals(0, summary.getCount());
        Assertions.assertEquals(0.0, summary.getAvgScore(), 0.01);
    }
}
