package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @PersistenceContext
    private EntityManager em;

    private long sellerId;
    private long otherSellerId;
    private long buyerId;
    private long otherBuyerId;
    private long productId;

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

    private Product insertProduct(final Long userId, final String title) {
        final Product product = new Product(
                userId,
                title,
                "Artist",
                "Label",
                "CAT",
                "Argentina",
                Collections.emptyList(),
                "Description",
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

    @BeforeEach
    public void setUp() {
        final User seller = insertUser("purchase-seller@test.com", "purchase-seller");
        final User otherSeller = insertUser("purchase-other-seller@test.com", "purchase-other-seller");
        final User buyer = insertUser("purchase-buyer@test.com", "purchase-buyer");
        final User otherBuyer = insertUser("purchase-other-buyer@test.com", "purchase-other-buyer");

        sellerId = seller.getId();
        otherSellerId = otherSeller.getId();
        buyerId = buyer.getId();
        otherBuyerId = otherBuyer.getId();

        final Product product = insertProduct(sellerId, "Purchase Album");
        productId = product.getId();
        em.flush();
        em.clear();
    }

    private long createProduct(long userId, String title) {
        final Product product = insertProduct(userId, title);
        return product.getId();
    }

    @Test
    public void testCreatePurchase() {
        // Arrange

        // Act
        final Purchase purchase = purchaseDao.createPurchase(
                productId, buyerId, sellerId, PurchaseStatus.PENDING, "buyer-token", "seller-token",
                LocalDateTime.now());
        em.flush();
        em.clear();

        // Assert
        Long count = em.createQuery(
                "SELECT COUNT(p) FROM Purchase p",
                Long.class).getSingleResult();

        Assertions.assertEquals(1L, count);
    }

    @Test
    public void testFindsById() {
        // Arrange
        final Purchase purchase = insertPurchase(
                productId, buyerId, sellerId, PurchaseStatus.PENDING, "buyer-token", "seller-token");
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
        final Purchase purchase = insertPurchase(
                productId, buyerId, sellerId, PurchaseStatus.PAID, "buyer-token", "seller-token");
        em.flush();

        // Act
        purchaseDao.updateStatus(purchase.getPurchaseId(), PurchaseStatus.DELIVERED);
        em.flush();
        em.clear();

        // Assert
        final Purchase reloaded = em.createQuery(
                "SELECT p FROM Purchase p WHERE p.purchaseId = :purchaseId",
                Purchase.class).setParameter("purchaseId", purchase.getPurchaseId()).getSingleResult();
        Assertions.assertEquals(PurchaseStatus.DELIVERED, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
        Assertions.assertTrue(reloaded.getConfirmed());
    }

    @Test
    public void testUpdateStatusFromPendingToPaidIsPersisted() {
        // Arrange
        final Purchase purchase = insertPurchase(
                productId, buyerId, sellerId, PurchaseStatus.PENDING, "buyer-token", "seller-token");
        em.flush();

        // Act
        purchaseDao.updateStatus(purchase.getPurchaseId(), PurchaseStatus.PAID);
        em.flush();
        em.clear();

        // Assert
        final Purchase reloaded = em.createQuery(
                "SELECT p FROM Purchase p WHERE p.purchaseId = :purchaseId",
                Purchase.class).setParameter("purchaseId", purchase.getPurchaseId()).getSingleResult();
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

        insertPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1");
        insertPurchase(secondProductId, buyerId, sellerId, PurchaseStatus.PAID, "b2", "s2");
        insertPurchase(otherBuyerProductId, otherBuyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3");
        em.flush();
        em.clear();

        // Act
        final PaginatedResult<Purchase> result = purchaseDao.findByBuyerId(buyerId, Collections.emptyList(), 1, 10);

        // Assert
        Assertions.assertEquals(2, result.getTotalCount());
        Assertions.assertEquals(1, result.getCurrentPage());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(2, result.getResults().size());
        Assertions.assertTrue(result.getResults().stream().allMatch(p -> p.getBuyerId() == buyerId));
    }

    @Test
    public void testFindBySellerIdFiltersPurchases() {
        // Arrange
        final long secondProductId = createProduct(sellerId, "Second Seller Album");
        final long otherSellerProductId = createProduct(otherSellerId, "Other Seller Album");

        insertPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1");
        insertPurchase(secondProductId, otherBuyerId, sellerId, PurchaseStatus.PAID, "b2", "s2");
        insertPurchase(otherSellerProductId, buyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3");
        em.flush();
        em.clear();

        // Act
        final List<Purchase> purchases = purchaseDao.findBySellerId(sellerId, Collections.emptyList(), 1, 10)
                .getResults();

        // Assert
        Assertions.assertEquals(2, purchases.size());
        Assertions.assertTrue(purchases.stream().allMatch(p -> p.getSellerId() == sellerId));
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
