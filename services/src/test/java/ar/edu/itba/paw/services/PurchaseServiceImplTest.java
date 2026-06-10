package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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

    @Mock
    private NotificationService notificationService;

    private PurchaseServiceImpl purchaseService;

    @BeforeEach
    public void setUp() {
        purchaseService = new PurchaseServiceImpl(
                purchaseDao,
                productService,
                userService,
                emailService,
                notificationService);
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
                LocalDate.now(),
                BigDecimal.valueOf(32000),
                1);
    }

    @Test
    public void createPurchaseReservesProductBeforeCreatingPurchase() {
        final Product product = product();
        final User seller = new User(
                SELLER_ID,
                "seller@test.com",
                "password",
                "seller",
                false,
                true,
                false,
                "Buyer",
                "User",
                "Main",
                "123",
                "Palermo",
                "CABA",
                null,
                null);
        final User buyer = new User(
                BUYER_ID,
                "buyer@test.com",
                "password",
                "buyer",
                false,
                true,
                false,
                "Buyer",
                "User",
                "Main",
                "123",
                "Palermo",
                "CABA",
                null,
                null);

        final Purchase purchase = new Purchase(99L, PRODUCT_ID, BUYER_ID, SELLER_ID, LocalDate.now(),
                PurchaseStatus.PENDING, "buyer-token", "seller-token");

        Mockito.when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(userService.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        Mockito.when(userService.findById(BUYER_ID)).thenReturn(Optional.of(buyer));
        Mockito.when(productService.decrementStock(PRODUCT_ID)).thenReturn(true);
        Mockito.when(purchaseDao.createPurchase(
                Mockito.eq(PRODUCT_ID),
                Mockito.eq(BUYER_ID),
                Mockito.eq(SELLER_ID),
                Mockito.eq(PurchaseStatus.PENDING),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(LocalDateTime.class))).thenReturn(purchase);
        final Purchase result = purchaseService.createPurchase(PRODUCT_ID, BUYER_ID);

        Assertions.assertSame(purchase, result);
        Mockito.verify(emailService).sendBuyerEmail(
                Mockito.same(purchase),
                Mockito.same(product),
                Mockito.same(buyer),
                Mockito.same(seller),
                Mockito.eq(PurchaseStatus.PENDING),
                Mockito.eq(buyer.getPreferredLocale()));
        Mockito.verify(emailService).sendSellerEmail(
                Mockito.same(purchase),
                Mockito.same(product),
                Mockito.same(buyer),
                Mockito.same(seller),
                Mockito.eq(PurchaseStatus.PENDING),
                Mockito.eq(seller.getPreferredLocale()));
    }

    @Test
    public void createPurchaseRejectsUnavailableProduct() {
        final Product product = product();
        final User seller = new User(
                SELLER_ID,
                "seller@test.com",
                "password",
                "seller",
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
        final User buyer = new User(
                BUYER_ID,
                "buyer@test.com",
                "password",
                "buyer",
                false,
                true,
                false,
                "Buyer",
                "User",
                "Main",
                "123",
                "Palermo",
                "CABA",
                null,
                null);

        Mockito.when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(userService.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        Mockito.when(userService.findById(BUYER_ID)).thenReturn(Optional.of(buyer));
        Mockito.when(productService.decrementStock(PRODUCT_ID)).thenReturn(false);

        Assertions.assertThrows(PurchaseCreationException.class,
                () -> purchaseService.createPurchase(PRODUCT_ID, BUYER_ID));
    }

    @Test
    public void createPurchaseRejectsIncompleteBuyerBeforeDecrementingStock() {
        final Product product = product();
        final User seller = new User(
                SELLER_ID,
                "seller@test.com",
                "password",
                "seller",
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
        final User incompleteBuyer = new User(
                BUYER_ID,
                "buyer@test.com",
                "password",
                "buyer",
                false,
                true,
                false,
                "Buyer",
                null,
                "Main",
                "123",
                "Palermo",
                "CABA",
                null,
                null);

        Mockito.when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(userService.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        Mockito.when(userService.findById(BUYER_ID)).thenReturn(Optional.of(incompleteBuyer));

        Assertions.assertThrows(PurchaseCreationException.class,
                () -> purchaseService.createPurchase(PRODUCT_ID, BUYER_ID));

        Mockito.verify(productService, Mockito.never()).decrementStock(Mockito.anyLong());
        Mockito.verify(purchaseDao, Mockito.never()).createPurchase(
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.any(PurchaseStatus.class),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(LocalDateTime.class));
    }

    @Test
    public void createPurchaseRejectsOwnProduct() {
        Mockito.when(productService.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));

        Assertions.assertThrows(PurchaseCreationException.class,
                () -> purchaseService.createPurchase(PRODUCT_ID, SELLER_ID));
    }
}
