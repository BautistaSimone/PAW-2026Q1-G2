package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class PurchaseJpaDaoTest {

    @Autowired
    private PurchaseJpaDao purchaseDao;

    @Autowired
    private UserJpaDao userDao;

    @Autowired
    private ProductJpaDao productDao;

    @PersistenceContext
    private EntityManager em;

    private long sellerId;
    private long otherSellerId;
    private long buyerId;
    private long otherBuyerId;
    private long productId;

    @BeforeEach
    public void setUp() {
        final User seller = userDao.createUser("purchase-seller@test.com", "pass", "purchase-seller",
            false, true, null, null, null, null, null, null, null, null);
        final User otherSeller = userDao.createUser("purchase-other-seller@test.com", "pass", "purchase-other-seller",
            false, true, null, null, null, null, null, null, null, null);
        final User buyer = userDao.createUser("purchase-buyer@test.com", "pass", "purchase-buyer",
            false, true, null, null, null, null, null, null, null, null);
        final User otherBuyer = userDao.createUser("purchase-other-buyer@test.com", "pass", "purchase-other-buyer",
            false, true, null, null, null, null, null, null, null, null);

        sellerId = seller.getId();
        otherSellerId = otherSeller.getId();
        buyerId = buyer.getId();
        otherBuyerId = otherBuyer.getId();

        final Product product = productDao.createProduct(
            sellerId, "Purchase Album", "Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000)
        );
        productId = product.getId();
        em.flush();
    }

    private long createProduct(long userId, String title) {
        final Product p = productDao.createProduct(
            userId, title, "Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000)
        );
        em.flush();
        return p.getId();
    }

    @Test
    public void testCreatePurchase() {
        // Arrange

        // Act
        final Purchase purchase = purchaseDao.createPurchase(
            productId, buyerId, sellerId, PurchaseStatus.PENDING, "buyer-token", "seller-token", LocalDateTime.now()
        );
        em.flush();
        em.clear();

        // Assert
        Long count = em.createQuery(
            "SELECT COUNT(p) FROM Purchase p",
            Long.class
        ).getSingleResult();

        Assertions.assertEquals(1L, count);
    }

    @Test
    public void testFindsById() {
        // Arrange
        final Purchase purchase = purchaseDao.createPurchase(
            productId, buyerId, sellerId, PurchaseStatus.PENDING, "buyer-token", "seller-token", LocalDateTime.now()
        );
        em.flush();
        em.clear();

        // Act
        final Purchase reloaded = purchaseDao.findById(purchase.getPurchaseId()).orElseThrow();

        // Assert
        Assertions.assertEquals(productId, reloaded.getProductId());
        Assertions.assertEquals(buyerId, reloaded.getBuyerId());
        Assertions.assertEquals(sellerId, reloaded.getSellerId());
        Assertions.assertEquals(PurchaseStatus.PENDING, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
    }

    @Test
    public void testUpdateStatusChangesStatusAndConfirmedFlag() {
        // Arrange
        final Purchase purchase = purchaseDao.createPurchase(
            productId, buyerId, sellerId, PurchaseStatus.PAID, "buyer-token", "seller-token", LocalDateTime.now()
        );
        em.flush();

        // Act
        purchaseDao.updateStatus(purchase.getPurchaseId(), PurchaseStatus.DELIVERED);
        em.flush();
        em.clear();

        // Assert
        final Purchase reloaded = purchaseDao.findById(purchase.getPurchaseId()).orElseThrow();
        Assertions.assertEquals(PurchaseStatus.DELIVERED, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
        Assertions.assertTrue(reloaded.getConfirmed());
    }

    @Test
    public void testUpdateStatusFromPendingToPaidIsPersisted() {
        // Arrange
        final Purchase purchase = purchaseDao.createPurchase(
            productId, buyerId, sellerId, PurchaseStatus.PENDING, "buyer-token", "seller-token", LocalDateTime.now()
        );
        em.flush();

        // Act
        purchaseDao.updateStatus(purchase.getPurchaseId(), PurchaseStatus.PAID);
        em.flush();
        em.clear();

        // Assert
        final Purchase reloaded = purchaseDao.findById(purchase.getPurchaseId()).orElseThrow();
        Assertions.assertEquals(PurchaseStatus.PAID, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
        Assertions.assertFalse(reloaded.getConfirmed());
    }

    @Test
    public void testFindByBuyerIdFiltersPurchases() {

        // Arrange
        final long secondProductId = createProduct(sellerId, "Second Purchase Album");
        final long otherBuyerProductId = createProduct(otherSellerId, "Other Buyer Album");

        purchaseDao.createPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1", LocalDateTime.now());
        purchaseDao.createPurchase(secondProductId, buyerId, sellerId, PurchaseStatus.PAID, "b2", "s2", LocalDateTime.now());
        purchaseDao.createPurchase(otherBuyerProductId, otherBuyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3", LocalDateTime.now());
        em.flush();

        // Act
        final PaginatedResult<Purchase> result = purchaseDao.findByBuyerId(buyerId, Collections.emptyList(), 1, 10);

        // Assert
        Assertions.assertEquals(2, result.getTotalCount());
        Assertions.assertEquals(1, result.getCurrentPage());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(2, result.getResults().size());
        Assertions.assertTrue(result.getResults().stream().allMatch(p -> p.getBuyerId().equals(buyerId)));
    }

    @Test
    public void testFindBySellerIdFiltersPurchases() {
        // Arrange
        final long secondProductId = createProduct(sellerId, "Second Seller Album");
        final long otherSellerProductId = createProduct(otherSellerId, "Other Seller Album");

        purchaseDao.createPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1", LocalDateTime.now());
        purchaseDao.createPurchase(secondProductId, otherBuyerId, sellerId, PurchaseStatus.PAID, "b2", "s2", LocalDateTime.now());
        purchaseDao.createPurchase(otherSellerProductId, buyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3", LocalDateTime.now());
        em.flush();

        // Act
        final List<Purchase> purchases = purchaseDao.findBySellerId(sellerId, Collections.emptyList(), 1, 10).getResults();

        // Assert
        Assertions.assertEquals(2, purchases.size());
        Assertions.assertTrue(purchases.stream().allMatch(p -> p.getSellerId().equals(sellerId)));
    }

    @Test
    public void testFindByBuyerIdReturnsEmptyPageWhenBuyerHasNoPurchases() {
        // Arrange

        // Act
        final PaginatedResult<Purchase> result = purchaseDao.findByBuyerId(buyerId, Collections.emptyList(), 1, 10);

        // Assert
        Assertions.assertTrue(result.getResults().isEmpty());
        Assertions.assertEquals(0, result.getTotalCount());
        Assertions.assertEquals(0, result.getTotalPages());
    }
}
