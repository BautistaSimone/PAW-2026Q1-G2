package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.ImageDao;
import ar.edu.itba.paw.persistence.ProductDao;
import ar.edu.itba.paw.persistence.ReportDao;
import ar.edu.itba.paw.persistence.UserDao;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    private ProductServiceImpl productService;

    @Mock
    private ProductDao productDao;

    @Mock
    private ImageDao imageDao;

    @Mock
    private ReportDao reportDao;

    @Mock
    private UserDao userDao;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PendingNotificationService pendingNotificationService;

    @Mock
    private NotificationService notificationService;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 2L;
    private static final byte[] COVER_BYTES = new byte[] { 1, 2, 3 };
    private static final byte[] DETAIL_BYTES = new byte[] { 4, 5, 6 };

    @BeforeEach
    public void setUp() {
        productService = new ProductServiceImpl(
            productDao,
            imageDao,
            reportDao,
            userDao,
            categoryService,
            pendingNotificationService,
            notificationService
        );
    }

    @Test
    public void createProductPersistsProductAndImagesInOneServiceCall() {
        final Product product = product(PRODUCT_ID, USER_ID);
        Mockito.when(userDao.findById(USER_ID)).thenReturn(Optional.of(sellerWithPaymentData(USER_ID)));
        Mockito.when(productDao.createProduct(
            Mockito.eq(USER_ID), Mockito.eq("Abbey Road"), Mockito.eq("The Beatles"), Mockito.eq("Apple Records"),
            Mockito.eq("PCS 7088"), Mockito.eq("UK"), Mockito.eq(Collections.emptyList()), Mockito.eq("Great condition"),
            Mockito.eq(new BigDecimal("8.5")), Mockito.eq(new BigDecimal("9.0")), Mockito.eq(new BigDecimal("1500.00")), Mockito.eq(2)
        )).thenReturn(product);

        final Product result = productService.createProduct(
            USER_ID,
            "Abbey Road",
            "The Beatles",
            "Apple Records",
            "PCS 7088",
            "UK",
            Collections.emptyList(),
            "Great condition",
            new BigDecimal("8.5"),
            new BigDecimal("9.0"),
            new BigDecimal("1500.00"),
            2,
            List.of(image(COVER_BYTES, "image/png"))
        );

        Assertions.assertSame(product, result);
        Mockito.verify(imageDao).createImage(
            Mockito.eq(PRODUCT_ID),
            Mockito.argThat(data -> Arrays.equals(data, COVER_BYTES)),
            Mockito.eq("image/png")
        );
        Mockito.verify(pendingNotificationService).enqueueForFollowers(USER_ID, PRODUCT_ID);
        Mockito.verify(notificationService).notifyNewProduct(USER_ID, PRODUCT_ID);
    }

    @Test
    public void createProductRejectsMissingImages() {
        Mockito.when(userDao.findById(USER_ID)).thenReturn(Optional.of(sellerWithPaymentData(USER_ID)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> productService.createProduct(
            USER_ID,
            "Title",
            "Artist",
            "Label",
            "Catalog",
            "Country",
            Collections.emptyList(),
            "Description",
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.ONE,
            1,
            Collections.emptyList()
        ));

        Mockito.verify(productDao, Mockito.never()).createProduct(
            Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.anyString(),
            Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.anyInt()
        );
    }

    @Test
    public void createProductRejectsPublisherWithoutSellerData() {
        Mockito.when(userDao.findById(USER_ID)).thenReturn(Optional.of(userWithoutSellerData(USER_ID)));

        Assertions.assertThrows(IllegalStateException.class, () -> productService.createProduct(
            USER_ID,
            "Title",
            "Artist",
            "Label",
            "Catalog",
            "Country",
            Collections.emptyList(),
            "Description",
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.ONE,
            1,
            List.of(image(COVER_BYTES, "image/png"))
        ));

        Mockito.verify(productDao, Mockito.never()).createProduct(
            Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.anyString(),
            Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.anyInt()
        );
    }

    @Test
    public void updateProductDoesNotTouchImagesWhenUnchanged() {
        final Product product = product(PRODUCT_ID, USER_ID);
        Mockito.when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(product), Optional.of(product));
        Mockito.when(userDao.findById(USER_ID)).thenReturn(Optional.of(sellerWithPaymentData(USER_ID)));
        Mockito.when(productDao.updateProduct(
            Mockito.eq(PRODUCT_ID), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.eq(Collections.emptyList()), Mockito.anyString(),
            Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.anyInt()
        )).thenReturn(true);

        final Product result = productService.updateProduct(
            USER_ID,
            PRODUCT_ID,
            "New Title",
            "New Artist",
            "New Label",
            "New Catalog",
            "New Country",
            Collections.emptyList(),
            "New Description",
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.ONE,
            1,
            ProductImageUpdate.unchanged()
        );

        Assertions.assertSame(product, result);
        Mockito.verify(imageDao, Mockito.never()).deleteByProductId(Mockito.anyLong());
        Mockito.verify(imageDao, Mockito.never()).createImage(Mockito.anyLong(), Mockito.any(), Mockito.anyString());
    }

    @Test
    public void updateProductReplacesImagesWithNewUploads() {
        final Product product = product(PRODUCT_ID, USER_ID);
        Mockito.when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(product), Optional.of(product));
        Mockito.when(userDao.findById(USER_ID)).thenReturn(Optional.of(sellerWithPaymentData(USER_ID)));
        Mockito.when(productDao.updateProduct(
            Mockito.eq(PRODUCT_ID), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.eq(Collections.emptyList()), Mockito.anyString(),
            Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.anyInt()
        )).thenReturn(true);

        productService.updateProduct(
            USER_ID,
            PRODUCT_ID,
            "New Title",
            "New Artist",
            "New Label",
            "New Catalog",
            "New Country",
            Collections.emptyList(),
            "New Description",
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.ONE,
            1,
            ProductImageUpdate.replaceWithNewImages(List.of(
                image(COVER_BYTES, "image/png"),
                image(DETAIL_BYTES, "image/jpeg")
            ))
        );

        final InOrder order = Mockito.inOrder(imageDao);
        order.verify(imageDao).deleteByProductId(PRODUCT_ID);
        order.verify(imageDao).createImage(
            Mockito.eq(PRODUCT_ID),
            Mockito.argThat(data -> Arrays.equals(data, COVER_BYTES)),
            Mockito.eq("image/png")
        );
        order.verify(imageDao).createImage(
            Mockito.eq(PRODUCT_ID),
            Mockito.argThat(data -> Arrays.equals(data, DETAIL_BYTES)),
            Mockito.eq("image/jpeg")
        );
    }

    @Test
    public void updateProductRebuildsLayoutFromExistingAndNewImages() {
        final Product product = product(PRODUCT_ID, USER_ID);
        final Image existingImage = new Image(10L, PRODUCT_ID, COVER_BYTES, "image/png");
        Mockito.when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(product), Optional.of(product));
        Mockito.when(userDao.findById(USER_ID)).thenReturn(Optional.of(sellerWithPaymentData(USER_ID)));
        Mockito.when(productDao.updateProduct(
            Mockito.eq(PRODUCT_ID), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.eq(Collections.emptyList()), Mockito.anyString(),
            Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.anyInt()
        )).thenReturn(true);
        Mockito.when(imageDao.findById(10L)).thenReturn(Optional.of(existingImage));

        productService.updateProduct(
            USER_ID,
            PRODUCT_ID,
            "New Title",
            "New Artist",
            "New Label",
            "New Catalog",
            "New Country",
            Collections.emptyList(),
            "New Description",
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.ONE,
            1,
            ProductImageUpdate.replaceWith(List.of(
                ProductImageUpdate.existingImage(10L),
                ProductImageUpdate.newImage(image(DETAIL_BYTES, "image/jpeg"))
            ))
        );

        final InOrder order = Mockito.inOrder(imageDao);
        order.verify(imageDao).findById(10L);
        order.verify(imageDao).deleteByProductId(PRODUCT_ID);
        order.verify(imageDao).createImage(
            Mockito.eq(PRODUCT_ID),
            Mockito.argThat(data -> Arrays.equals(data, COVER_BYTES)),
            Mockito.eq("image/png")
        );
        order.verify(imageDao).createImage(
            Mockito.eq(PRODUCT_ID),
            Mockito.argThat(data -> Arrays.equals(data, DETAIL_BYTES)),
            Mockito.eq("image/jpeg")
        );
    }

    @Test
    public void updateProductRejectsExistingImageFromAnotherProduct() {
        final Product product = product(PRODUCT_ID, USER_ID);
        final Image foreignImage = new Image(10L, 999L, COVER_BYTES, "image/png");
        Mockito.when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(userDao.findById(USER_ID)).thenReturn(Optional.of(sellerWithPaymentData(USER_ID)));
        Mockito.when(productDao.updateProduct(
            Mockito.eq(PRODUCT_ID), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.eq(Collections.emptyList()), Mockito.anyString(),
            Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.anyInt()
        )).thenReturn(true);
        Mockito.when(imageDao.findById(10L)).thenReturn(Optional.of(foreignImage));

        Assertions.assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(
            USER_ID,
            PRODUCT_ID,
            "New Title",
            "New Artist",
            "New Label",
            "New Catalog",
            "New Country",
            Collections.emptyList(),
            "New Description",
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.ONE,
            1,
            ProductImageUpdate.replaceWith(List.of(ProductImageUpdate.existingImage(10L)))
        ));

        Mockito.verify(imageDao, Mockito.never()).deleteByProductId(Mockito.anyLong());
    }

    @Test
    public void hideProductByAdminAlsoDeletesReports() {
        productService.hideProductByAdmin(PRODUCT_ID);

        Mockito.verify(productDao).markAsAdminHidden(PRODUCT_ID);
        Mockito.verify(reportDao).deleteByProductId(PRODUCT_ID);
    }

    @Test
    public void hideAllProductsByAdminAlsoDeletesOwnerReports() {
        Mockito.when(productDao.markAllAsAdminHiddenByUserId(USER_ID)).thenReturn(3);

        final int hidden = productService.hideAllProductsByAdmin(USER_ID);

        Assertions.assertEquals(3, hidden);
        Mockito.verify(productDao).markAllAsAdminHiddenByUserId(USER_ID);
        Mockito.verify(reportDao).deleteByOwnerUserId(USER_ID);
    }

    @Test
    public void getProductSearchCriteriaNormalizesPriceBounds() {
        final ProductSearchCriteria criteria = productService.getProductSearchCriteria(
            "   Abbey Road  ",
            List.of(3L),
            new BigDecimal("500"),
            new BigDecimal("100"),
            List.of("Label"),
            Collections.emptyList(),
            ProductSortOrder.PRICE_ASC,
            1
        );

        Assertions.assertEquals("Abbey Road", criteria.getSearchText());
        Assertions.assertEquals(new BigDecimal("100"), criteria.getMinPrice());
        Assertions.assertEquals(new BigDecimal("500"), criteria.getMaxPrice());
        Assertions.assertEquals(ProductSortOrder.PRICE_ASC, criteria.getSortOrder());
        Assertions.assertEquals(ProductSearchCriteria.DEFAULT_PAGE_SIZE, criteria.getPageSize());
    }

    @Test
    public void listActiveProductsByUserWithNullUserIdReturnsEmptyPage() {
        final PaginatedResult<Product> result = productService.listActiveProductsByUser(null, 1, 10);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getResults().isEmpty());
        Assertions.assertEquals(0, result.getTotalCount());
    }

    private static ProductImageData image(final byte[] data, final String contentType) {
        return new ProductImageData(data, contentType);
    }

    private static Product product(final Long productId, final Long userId) {
        return new Product(
            productId,
            userId,
            "Title",
            "Artist",
            "Label",
            "Catalog",
            "Country",
            Collections.emptyList(),
            "Description",
            BigDecimal.TEN,
            BigDecimal.TEN,
            LocalDate.now(),
            BigDecimal.ONE,
            1
        );
    }

    private static User sellerWithPaymentData(final Long userId) {
        return new User(
            userId,
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
            "Palermo",
            "CABA",
            null,
            "1234567890123456789012"
        );
    }

    private static User userWithoutSellerData(final Long userId) {
        return new User(
            userId,
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
            null
        );
    }
}
