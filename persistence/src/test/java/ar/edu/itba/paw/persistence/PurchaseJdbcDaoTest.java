package ar.edu.itba.paw.persistence;

import java.util.List;

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

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class PurchaseJdbcDaoTest {

    @Autowired
    private PurchaseJdbcDao purchaseDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private long sellerId;
    private long otherSellerId;
    private long buyerId;
    private long otherBuyerId;
    private long productId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        sellerId = insertUser("purchase-seller");
        otherSellerId = insertUser("purchase-other-seller");
        buyerId = insertUser("purchase-buyer");
        otherBuyerId = insertUser("purchase-other-buyer");
        productId = insertProduct(sellerId, "Purchase Album");
    }

    private long insertUser(final String suffix) {
        jdbcTemplate.update(
            "INSERT INTO users (email, password, username, mod) VALUES (?, 'pass', ?, false)",
            suffix + "@test.com",
            suffix
        );
        return jdbcTemplate.queryForObject("CALL IDENTITY()", Long.class);
    }

    private long insertProduct(final long userId, final String title) {
        jdbcTemplate.update(
            "INSERT INTO products (user_id, title, artist, description, sleeve_condition, record_condition, published, price) "
                + "VALUES (?, ?, 'Artist', 'Description', 8, 9, CURRENT_DATE, 1000)",
            userId,
            title
        );
        return jdbcTemplate.queryForObject("CALL IDENTITY()", Long.class);
    }

    @Test
    public void createPurchasePersistsPurchaseAndFindsItById() {
        final Purchase purchase = purchaseDao.createPurchase(
            productId,
            buyerId,
            sellerId,
            PurchaseStatus.PENDING,
            "buyer-token",
            "seller-token"
        );

        final Purchase reloaded = purchaseDao.findById(purchase.getPurchaseId()).orElseThrow();

        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "purchases"));
        Assertions.assertEquals(productId, reloaded.getProductId());
        Assertions.assertEquals(buyerId, reloaded.getBuyerId());
        Assertions.assertEquals(sellerId, reloaded.getSellerId());
        Assertions.assertEquals(PurchaseStatus.PENDING, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
    }

    @Test
    public void updateStatusChangesStatusAndConfirmedFlagWithoutChangingTokens() {
        final Purchase purchase = purchaseDao.createPurchase(
            productId,
            buyerId,
            sellerId,
            PurchaseStatus.PAID,
            "buyer-token",
            "seller-token"
        );

        purchaseDao.updateStatus(purchase.getPurchaseId(), PurchaseStatus.DELIVERED);

        final Purchase reloaded = purchaseDao.findById(purchase.getPurchaseId()).orElseThrow();
        final Boolean confirmed = jdbcTemplate.queryForObject(
            "SELECT confirmed FROM purchases WHERE purchase_id = ?",
            Boolean.class,
            purchase.getPurchaseId()
        );
        Assertions.assertEquals(PurchaseStatus.DELIVERED, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
        Assertions.assertEquals(Boolean.TRUE, confirmed);
    }

    @Test
    public void findByBuyerIdFiltersPurchasesAndReportsPaginationMetadata() {
        final long secondProductId = insertProduct(sellerId, "Second Purchase Album");
        final long otherBuyerProductId = insertProduct(otherSellerId, "Other Buyer Album");
        purchaseDao.createPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1");
        purchaseDao.createPurchase(secondProductId, buyerId, sellerId, PurchaseStatus.PAID, "b2", "s2");
        purchaseDao.createPurchase(otherBuyerProductId, otherBuyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3");

        final PaginatedResult<Purchase> result = purchaseDao.findByBuyerId(buyerId, 1, 10);

        Assertions.assertEquals(2, result.getTotalCount());
        Assertions.assertEquals(1, result.getCurrentPage());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(2, result.getResults().size());
        Assertions.assertTrue(result.getResults().stream().allMatch(purchase -> purchase.getBuyerId().equals(buyerId)));
    }

    @Test
    public void findBySellerIdFiltersPurchases() {
        final long secondProductId = insertProduct(sellerId, "Second Seller Album");
        final long otherSellerProductId = insertProduct(otherSellerId, "Other Seller Album");
        purchaseDao.createPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1");
        purchaseDao.createPurchase(secondProductId, otherBuyerId, sellerId, PurchaseStatus.PAID, "b2", "s2");
        purchaseDao.createPurchase(otherSellerProductId, buyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3");

        final List<Purchase> purchases = purchaseDao.findBySellerId(sellerId, 1, 10).getResults();

        Assertions.assertEquals(2, purchases.size());
        Assertions.assertTrue(purchases.stream().allMatch(purchase -> purchase.getSellerId().equals(sellerId)));
    }

    @Test
    public void findByBuyerIdReturnsEmptyPageWhenBuyerHasNoPurchases() {
        final PaginatedResult<Purchase> result = purchaseDao.findByBuyerId(buyerId, 1, 10);

        Assertions.assertTrue(result.getResults().isEmpty());
        Assertions.assertEquals(0, result.getTotalCount());
        Assertions.assertEquals(0, result.getTotalPages());
    }
}
