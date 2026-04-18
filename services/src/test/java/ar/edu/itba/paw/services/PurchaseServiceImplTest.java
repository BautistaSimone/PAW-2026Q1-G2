package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.PurchaseDao;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTest {

    private static final long PRODUCT_ID = 10L;
    private static final long SELLER_ID = 1L;
    private static final long BUYER_ID = 2L;

    @Mock
    private PurchaseDao purchaseDao;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    private PurchaseServiceImpl purchaseService;

    @BeforeEach
    public void setUp() {
        purchaseService = new PurchaseServiceImpl(purchaseDao, productService, userService, emailService);
    }

    private static Product product() {
        return new Product(
            PRODUCT_ID,
            SELLER_ID,
            "Dynamo",
            "Soda Stereo",
            "Sony",
            "EPC 85930",
            "Argentina",
            Collections.emptyList(),
            "Edicion original",
            BigDecimal.valueOf(9),
            BigDecimal.valueOf(9),
            "Palermo",
            "CABA",
            LocalDate.now(),
            BigDecimal.valueOf(32000)
        );
    }

    @Test
    public void createPurchaseReservesProductBeforeCreatingPurchase() {
        final Product product = product();
        final User seller = new User(SELLER_ID, "seller@test.com", "password", "seller", false);
        final User buyer = new User(BUYER_ID, "buyer@test.com", "password", "buyer", false);
        final Purchase purchase = new Purchase(99L, PRODUCT_ID, BUYER_ID, LocalDate.now(), PurchaseStatus.PENDING, "buyer-token", "seller-token");

        Mockito.when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(userService.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        Mockito.when(userService.findById(BUYER_ID)).thenReturn(Optional.of(buyer));
        Mockito.when(productService.reserveIfAvailable(PRODUCT_ID)).thenReturn(true);
        Mockito.when(purchaseDao.createPurchase(
            Mockito.eq(PRODUCT_ID),
            Mockito.eq(BUYER_ID),
            Mockito.eq(SELLER_ID),
            Mockito.eq(PurchaseStatus.PENDING),
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(purchase);

        final Purchase result = purchaseService.createPurchase(PRODUCT_ID, BUYER_ID);

        Assertions.assertSame(purchase, result);
        final InOrder inOrder = Mockito.inOrder(productService, purchaseDao);
        inOrder.verify(productService).findById(PRODUCT_ID);
        inOrder.verify(productService).reserveIfAvailable(PRODUCT_ID);
        inOrder.verify(purchaseDao).createPurchase(
            Mockito.eq(PRODUCT_ID),
            Mockito.eq(BUYER_ID),
            Mockito.eq(SELLER_ID),
            Mockito.eq(PurchaseStatus.PENDING),
            Mockito.anyString(),
            Mockito.anyString()
        );
        Mockito.verify(emailService).sendBuyerEmail(
            Mockito.eq("buyer@test.com"),
            Mockito.eq(purchase),
            Mockito.eq(product),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.eq("buyer"),
            Mockito.eq(PurchaseStatus.PENDING)
        );
    }

    @Test
    public void createPurchaseRejectsUnavailableProduct() {
        final Product product = product();
        final User seller = new User(SELLER_ID, "seller@test.com", "password", "seller", false);
        final User buyer = new User(BUYER_ID, "buyer@test.com", "password", "buyer", false);

        Mockito.when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(userService.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        Mockito.when(userService.findById(BUYER_ID)).thenReturn(Optional.of(buyer));
        Mockito.when(productService.reserveIfAvailable(PRODUCT_ID)).thenReturn(false);

        Assertions.assertThrows(IllegalStateException.class, () ->
            purchaseService.createPurchase(PRODUCT_ID, BUYER_ID)
        );
        Mockito.verify(purchaseDao, Mockito.never()).createPurchase(
            Mockito.anyLong(),
            Mockito.anyLong(),
            Mockito.anyLong(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyString()
        );
        Mockito.verifyNoInteractions(emailService);
    }

    @Test
    public void createPurchaseRejectsOwnProduct() {
        Mockito.when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            purchaseService.createPurchase(PRODUCT_ID, SELLER_ID)
        );
        Mockito.verify(productService, Mockito.never()).reserveIfAvailable(Mockito.anyLong());
        Mockito.verifyNoInteractions(purchaseDao, emailService);
    }
}
