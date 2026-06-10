package ar.edu.itba.paw.services;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.User;

@ExtendWith(MockitoExtension.class)
public class PurchaseDisplayServiceImplTest {

    private static final long PURCHASE_ID = 99L;
    private static final long PRODUCT_ID = 10L;
    private static final long BUYER_ID = 1L;
    private static final long SELLER_ID = 2L;
    private static final long USER_ID = BUYER_ID;

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private ReviewService reviewService;

    private PurchaseDisplayServiceImpl purchaseDisplayService;

    @BeforeEach
    public void setUp() {
        purchaseDisplayService = new PurchaseDisplayServiceImpl(
                purchaseService,
                reviewService);
    }

    @Test
    public void getPurchaseDisplayReturnsDisplayWithReviewWhenDeliveredAndReviewed() {
        final Purchase purchase = new Purchase(PURCHASE_ID, PRODUCT_ID, BUYER_ID, SELLER_ID, LocalDate.now(),
                PurchaseStatus.DELIVERED, "buyer-token", "seller-token");
        final Product product = new Product(PRODUCT_ID, SELLER_ID, "Album", "Artist", "Label", "CAT", "Country",
                java.util.Collections.emptyList(), "Desc", null, null, null, null, 1);
        final User buyer = new User(BUYER_ID, "buyer@test.com", "pass", "buyer", false, true, false,
                "John", "Doe", "Main", "123", "Palermo", "CABA", null, null);
        final User seller = new User(SELLER_ID, "seller@test.com", "pass", "seller", false, true, false,
                "Jane", "Doe", "Oak", "456", "Belgrano", "CABA", null, null);

        final PurchaseService.PurchaseDetails details = new PurchaseService.PurchaseDetails(
                purchase, product, buyer, seller, true, false);

        Mockito.when(purchaseService.getPurchaseDetailsForUser(PURCHASE_ID, USER_ID)).thenReturn(details);
        Mockito.when(reviewService.findByPurchaseId(PURCHASE_ID)).thenReturn(Optional.of(
                new Review(1L, PURCHASE_ID, SELLER_ID, BUYER_ID, 5, "Great!", null, "buyer")));

        final PurchaseDisplayService.PurchaseDisplay result = purchaseDisplayService.getPurchaseDisplay(PURCHASE_ID, USER_ID);

        Assertions.assertSame(purchase, result.getPurchase());
        Assertions.assertSame(product, result.getProduct());
        Assertions.assertSame(buyer, result.getOrderBuyer());
        Assertions.assertSame(seller, result.getOrderSeller());
        Assertions.assertTrue(result.isBuyer());
        Assertions.assertFalse(result.isSeller());
        Assertions.assertTrue(result.isHasReview());
    }

    @Test
    public void getPurchaseDisplayReturnsDisplayWithoutReviewWhenNotDelivered() {
        final Purchase purchase = new Purchase(PURCHASE_ID, PRODUCT_ID, BUYER_ID, SELLER_ID, LocalDate.now(),
                PurchaseStatus.SHIPPED, "buyer-token", "seller-token");
        final Product product = new Product(PRODUCT_ID, SELLER_ID, "Album", "Artist", "Label", "CAT", "Country",
                java.util.Collections.emptyList(), "Desc", null, null, null, null, 1);
        final User buyer = new User(BUYER_ID, "buyer@test.com", "pass", "buyer", false, true, false,
                "John", "Doe", "Main", "123", "Palermo", "CABA", null, null);
        final User seller = new User(SELLER_ID, "seller@test.com", "pass", "seller", false, true, false,
                "Jane", "Doe", "Oak", "456", "Belgrano", "CABA", null, null);

        final PurchaseService.PurchaseDetails details = new PurchaseService.PurchaseDetails(
                purchase, product, buyer, seller, true, false);

        Mockito.when(purchaseService.getPurchaseDetailsForUser(PURCHASE_ID, USER_ID)).thenReturn(details);

        final PurchaseDisplayService.PurchaseDisplay result = purchaseDisplayService.getPurchaseDisplay(PURCHASE_ID, USER_ID);

        Assertions.assertFalse(result.isHasReview());
        Mockito.verifyNoInteractions(reviewService);
    }

    @Test
    public void getPurchaseDisplayReturnsDisplayWithoutReviewWhenNotBuyerView() {
        final Purchase purchase = new Purchase(PURCHASE_ID, PRODUCT_ID, BUYER_ID, SELLER_ID, LocalDate.now(),
                PurchaseStatus.DELIVERED, "buyer-token", "seller-token");
        final Product product = new Product(PRODUCT_ID, SELLER_ID, "Album", "Artist", "Label", "CAT", "Country",
                java.util.Collections.emptyList(), "Desc", null, null, null, null, 1);
        final User buyer = new User(BUYER_ID, "buyer@test.com", "pass", "buyer", false, true, false,
                "John", "Doe", "Main", "123", "Palermo", "CABA", null, null);
        final User seller = new User(SELLER_ID, "seller@test.com", "pass", "seller", false, true, false,
                "Jane", "Doe", "Oak", "456", "Belgrano", "CABA", null, null);

        final PurchaseService.PurchaseDetails details = new PurchaseService.PurchaseDetails(
                purchase, product, buyer, seller, false, true);

        Mockito.when(purchaseService.getPurchaseDetailsForUser(PURCHASE_ID, SELLER_ID)).thenReturn(details);

        final PurchaseDisplayService.PurchaseDisplay result = purchaseDisplayService.getPurchaseDisplay(PURCHASE_ID, SELLER_ID);

        Assertions.assertFalse(result.isHasReview());
        Assertions.assertTrue(result.isSeller());
        Assertions.assertFalse(result.isBuyer());
        Mockito.verifyNoInteractions(reviewService);
    }

    @Test
    public void getPurchaseDisplayReturnsDisplayWithSellerViewFlags() {
        final Purchase purchase = new Purchase(PURCHASE_ID, PRODUCT_ID, BUYER_ID, SELLER_ID, LocalDate.now(),
                PurchaseStatus.PAID, "buyer-token", "seller-token");
        final Product product = new Product(PRODUCT_ID, SELLER_ID, "Album", "Artist", "Label", "CAT", "Country",
                java.util.Collections.emptyList(), "Desc", null, null, null, null, 1);
        final User buyer = new User(BUYER_ID, "buyer@test.com", "pass", "buyer", false, true, false,
                "John", "Doe", "Main", "123", "Palermo", "CABA", null, null);
        final User seller = new User(SELLER_ID, "seller@test.com", "pass", "seller", false, true, false,
                "Jane", "Doe", "Oak", "456", "Belgrano", "CABA", null, null);

        final PurchaseService.PurchaseDetails details = new PurchaseService.PurchaseDetails(
                purchase, product, buyer, seller, false, true);

        Mockito.when(purchaseService.getPurchaseDetailsForUser(PURCHASE_ID, SELLER_ID)).thenReturn(details);

        final PurchaseDisplayService.PurchaseDisplay result = purchaseDisplayService.getPurchaseDisplay(PURCHASE_ID, SELLER_ID);

        Assertions.assertFalse(result.isBuyer());
        Assertions.assertTrue(result.isSeller());
        Assertions.assertFalse(result.isHasReview());
        Assertions.assertFalse(result.isHasPaymentProof());
        Mockito.verifyNoInteractions(reviewService);
    }
}