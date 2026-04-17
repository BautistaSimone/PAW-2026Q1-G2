package ar.edu.itba.paw.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.persistence.ReviewDao;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private ProductService productService;

    private Purchase makePurchase(PurchaseStatus status, long buyerId) {
        return new Purchase(1L, 10L, buyerId, LocalDate.now(), status, "bt", "st");
    }

    private Product makeProduct(long userId) {
        return new Product(10L, userId, "Title", "Artist", "", "", "",
            java.util.Collections.emptyList(), "desc",
            java.math.BigDecimal.ONE, java.math.BigDecimal.ONE,
            "Palermo", "CABA", LocalDate.now(), java.math.BigDecimal.TEN);
    }

    @Test
    public void testCreateReviewSuccessfully() {
        Purchase purchase = makePurchase(PurchaseStatus.DELIVERED, 2L);
        Product product = makeProduct(3L);
        Review expected = new Review(1L, 1L, 3L, 2L, 5, "Great", LocalDateTime.now(), null);

        Mockito.when(purchaseService.findById(1L)).thenReturn(Optional.of(purchase));
        Mockito.when(reviewDao.findByPurchaseId(1L)).thenReturn(Optional.empty());
        Mockito.when(productService.findById(10L)).thenReturn(Optional.of(product));
        Mockito.when(reviewDao.create(1L, 3L, 2L, 5, "Great")).thenReturn(expected);

        Review result = reviewService.create(1L, 2L, 5, "Great");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(5, result.getScore());
    }

    @Test
    public void testRejectReviewWhenNotDelivered() {
        Purchase purchase = makePurchase(PurchaseStatus.SHIPPED, 2L);
        Mockito.when(purchaseService.findById(1L)).thenReturn(Optional.of(purchase));

        Assertions.assertThrows(IllegalStateException.class, () ->
            reviewService.create(1L, 2L, 5, "text")
        );
    }

    @Test
    public void testRejectReviewWhenNotBuyer() {
        Purchase purchase = makePurchase(PurchaseStatus.DELIVERED, 2L);
        Mockito.when(purchaseService.findById(1L)).thenReturn(Optional.of(purchase));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            reviewService.create(1L, 999L, 5, "text")
        );
    }

    @Test
    public void testRejectDuplicateReview() {
        Purchase purchase = makePurchase(PurchaseStatus.DELIVERED, 2L);
        Mockito.when(purchaseService.findById(1L)).thenReturn(Optional.of(purchase));
        Mockito.when(reviewDao.findByPurchaseId(1L)).thenReturn(
            Optional.of(new Review(1L, 1L, 3L, 2L, 5, "Old", LocalDateTime.now(), null))
        );

        Assertions.assertThrows(IllegalStateException.class, () ->
            reviewService.create(1L, 2L, 4, "New")
        );
    }
}
