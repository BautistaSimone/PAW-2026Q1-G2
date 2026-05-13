package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
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
    public void createPurchasePersistsPurchaseAndFindsItById() {
        final Purchase purchase = purchaseDao.createPurchase(
            productId, buyerId, sellerId, PurchaseStatus.PENDING, "buyer-token", "seller-token"
        );
        em.flush();
        em.clear();

        final Purchase reloaded = purchaseDao.findById(purchase.getPurchaseId()).orElseThrow();

        Assertions.assertEquals(productId, reloaded.getProductId());
        Assertions.assertEquals(buyerId, reloaded.getBuyerId());
        Assertions.assertEquals(sellerId, reloaded.getSellerId());
        Assertions.assertEquals(PurchaseStatus.PENDING, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
    }

    @Test
    public void updateStatusChangesStatusAndConfirmedFlag() {
        final Purchase purchase = purchaseDao.createPurchase(
            productId, buyerId, sellerId, PurchaseStatus.PAID, "buyer-token", "seller-token"
        );
        em.flush();

        purchaseDao.updateStatus(purchase.getPurchaseId(), PurchaseStatus.DELIVERED);
        em.flush();
        em.clear();

        final Purchase reloaded = purchaseDao.findById(purchase.getPurchaseId()).orElseThrow();
        Assertions.assertEquals(PurchaseStatus.DELIVERED, reloaded.getStatus());
        Assertions.assertEquals("buyer-token", reloaded.getBuyerToken());
        Assertions.assertEquals("seller-token", reloaded.getSellerToken());
        Assertions.assertTrue(reloaded.getConfirmed());
    }

    @Test
    public void findByBuyerIdFiltersPurchases() {
        final long secondProductId = createProduct(sellerId, "Second Purchase Album");
        final long otherBuyerProductId = createProduct(otherSellerId, "Other Buyer Album");

        purchaseDao.createPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1");
        purchaseDao.createPurchase(secondProductId, buyerId, sellerId, PurchaseStatus.PAID, "b2", "s2");
        purchaseDao.createPurchase(otherBuyerProductId, otherBuyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3");
        em.flush();

        final PaginatedResult<Purchase> result = purchaseDao.findByBuyerId(buyerId, Collections.emptyList(), 1, 10);

        Assertions.assertEquals(2, result.getTotalCount());
        Assertions.assertEquals(1, result.getCurrentPage());
        Assertions.assertEquals(1, result.getTotalPages());
        Assertions.assertEquals(2, result.getResults().size());
        Assertions.assertTrue(result.getResults().stream().allMatch(p -> p.getBuyerId().equals(buyerId)));
    }

    @Test
    public void findBySellerIdFiltersPurchases() {
        final long secondProductId = createProduct(sellerId, "Second Seller Album");
        final long otherSellerProductId = createProduct(otherSellerId, "Other Seller Album");

        purchaseDao.createPurchase(productId, buyerId, sellerId, PurchaseStatus.PENDING, "b1", "s1");
        purchaseDao.createPurchase(secondProductId, otherBuyerId, sellerId, PurchaseStatus.PAID, "b2", "s2");
        purchaseDao.createPurchase(otherSellerProductId, buyerId, otherSellerId, PurchaseStatus.PAID, "b3", "s3");
        em.flush();

        final List<Purchase> purchases = purchaseDao.findBySellerId(sellerId, Collections.emptyList(), 1, 10).getResults();

        Assertions.assertEquals(2, purchases.size());
        Assertions.assertTrue(purchases.stream().allMatch(p -> p.getSellerId().equals(sellerId)));
    }

    @Test
    public void findByBuyerIdReturnsEmptyPageWhenBuyerHasNoPurchases() {
        final PaginatedResult<Purchase> result = purchaseDao.findByBuyerId(buyerId, Collections.emptyList(), 1, 10);

        Assertions.assertTrue(result.getResults().isEmpty());
        Assertions.assertEquals(0, result.getTotalCount());
        Assertions.assertEquals(0, result.getTotalPages());
    }
}
