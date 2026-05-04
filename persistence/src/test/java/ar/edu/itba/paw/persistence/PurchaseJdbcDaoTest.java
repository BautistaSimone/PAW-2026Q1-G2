package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.Collections;
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
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class PurchaseJdbcDaoTest {

    @Autowired
    private PurchaseJdbcDao purchaseDao;

    @Autowired
    private ProductJdbcDao productDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private User createUser(final String email) {
        return userDao.createUser(email, "password", email, false, true, null, null, null, null, null, null, null, null);
    }

    private Product createProduct(final User seller, final String title) {
        return productDao.createProduct(
            seller.getId(),
            title,
            "Artist",
            "Label",
            "CAT-001",
            "Argentina",
            Collections.emptyList(),
            "Description",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(1000)
        );
    }

    @Test
    public void createPurchasePersistsAndFindByIdReturnsStoredTokensAndStatus() {
        final User seller = createUser("purchase-seller@test.com");
        final User buyer = createUser("purchase-buyer@test.com");
        final Product product = createProduct(seller, "Purchased Album");

        final Purchase created = purchaseDao.createPurchase(
            product.getId(),
            buyer.getId(),
            seller.getId(),
            PurchaseStatus.PENDING,
            "buyer-token",
            "seller-token"
        );

        final Purchase found = purchaseDao.findById(created.getPurchaseId()).orElseThrow();
        Assertions.assertEquals(product.getId(), found.getProductId());
        Assertions.assertEquals(buyer.getId(), found.getBuyerId());
        Assertions.assertEquals(seller.getId(), found.getSellerId());
        Assertions.assertEquals(PurchaseStatus.PENDING, found.getStatus());
        Assertions.assertEquals("buyer-token", found.getBuyerToken());
        Assertions.assertEquals("seller-token", found.getSellerToken());
    }

    @Test
    public void updateStatusPreservesTokensAndMarksDeliveredPurchaseAsConfirmed() {
        final User seller = createUser("purchase-update-seller@test.com");
        final User buyer = createUser("purchase-update-buyer@test.com");
        final Product product = createProduct(seller, "Delivered Album");
        final Purchase created = purchaseDao.createPurchase(
            product.getId(),
            buyer.getId(),
            seller.getId(),
            PurchaseStatus.PAID,
            "buyer-secret",
            "seller-secret"
        );

        purchaseDao.updateStatus(created.getPurchaseId(), PurchaseStatus.DELIVERED);

        final Purchase updated = purchaseDao.findById(created.getPurchaseId()).orElseThrow();
        final Boolean confirmed = jdbcTemplate.queryForObject(
            "SELECT confirmed FROM purchases WHERE purchase_id = ?",
            Boolean.class,
            created.getPurchaseId()
        );
        Assertions.assertEquals(PurchaseStatus.DELIVERED, updated.getStatus());
        Assertions.assertEquals("buyer-secret", updated.getBuyerToken());
        Assertions.assertEquals("seller-secret", updated.getSellerToken());
        Assertions.assertEquals(Boolean.TRUE, confirmed);
    }

    @Test
    public void findByBuyerIdAndSellerIdReturnOnlyMatchingPurchases() {
        final User seller = createUser("purchase-filter-seller@test.com");
        final User otherSeller = createUser("purchase-filter-other-seller@test.com");
        final User buyer = createUser("purchase-filter-buyer@test.com");
        final User otherBuyer = createUser("purchase-filter-other-buyer@test.com");
        final Product firstProduct = createProduct(seller, "First Album");
        final Product secondProduct = createProduct(seller, "Second Album");
        final Product thirdProduct = createProduct(otherSeller, "Third Album");
        purchaseDao.createPurchase(firstProduct.getId(), buyer.getId(), seller.getId(), PurchaseStatus.PENDING, "b1", "s1");
        purchaseDao.createPurchase(secondProduct.getId(), buyer.getId(), seller.getId(), PurchaseStatus.PAID, "b2", "s2");
        purchaseDao.createPurchase(thirdProduct.getId(), otherBuyer.getId(), otherSeller.getId(), PurchaseStatus.PAID, "b3", "s3");

        final PaginatedResult<Purchase> byBuyer = purchaseDao.findByBuyerId(buyer.getId(), 1, 10);
        final PaginatedResult<Purchase> bySeller = purchaseDao.findBySellerId(seller.getId(), 1, 10);

        Assertions.assertEquals(2, byBuyer.getTotalCount());
        Assertions.assertEquals(2, byBuyer.getResults().size());
        Assertions.assertTrue(byBuyer.getResults().stream().allMatch(p -> p.getBuyerId().equals(buyer.getId())));
        Assertions.assertEquals(2, bySeller.getTotalCount());
        Assertions.assertEquals(2, bySeller.getResults().size());
        Assertions.assertTrue(bySeller.getResults().stream().allMatch(p -> p.getSellerId().equals(seller.getId())));
    }

    @Test
    public void findByBuyerIdReturnsEmptyPageWhenBuyerHasNoPurchases() {
        final PaginatedResult<Purchase> page = purchaseDao.findByBuyerId(999L, 1, 10);

        Assertions.assertTrue(page.getResults().isEmpty());
        Assertions.assertEquals(0, page.getTotalCount());
        Assertions.assertEquals(0, page.getTotalPages());
    }
}
