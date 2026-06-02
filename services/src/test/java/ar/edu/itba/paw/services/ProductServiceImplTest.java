package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.persistence.ProductDao;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductDao productDao;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PendingNotificationService pendingNotificationService;

    @Mock
    private NotificationService notificationService;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 2L;

    @Test
    public void testCreateProductWithValidParams() {
        // Arrange
        final String title = "Abbey Road";
        final String artist = "The Beatles";
        final String label = "Apple Records";
        final String catalog = "PCS 7088";
        final String country = "UK";
        final List<Long> categoryIds = List.of(3L);
        final String desc = "Great condition retro vinyl album";
        final BigDecimal sleeveCond = new BigDecimal("8.5");
        final BigDecimal recordCond = new BigDecimal("9.0");
        final BigDecimal price = new BigDecimal("1500.00");
        final int stock = 2;

        final Category category = Mockito.mock(Category.class);
        Mockito.when(category.getId()).thenReturn(3L);
        Mockito.when(categoryService.findByIds(List.of(3L))).thenReturn(List.of(category));

        final Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockProduct.getId()).thenReturn(PRODUCT_ID);

        Mockito.when(productDao.createProduct(
            Mockito.eq(USER_ID), Mockito.eq(title), Mockito.eq(artist), Mockito.eq("Apple Records"),
            Mockito.eq(catalog), Mockito.eq(country), Mockito.anyList(), Mockito.eq(desc),
            Mockito.eq(sleeveCond), Mockito.eq(recordCond), Mockito.eq(price), Mockito.eq(stock)
        )).thenReturn(mockProduct);

        // Act
        final Product result = productService.createProduct(
            USER_ID, title, artist, label, catalog, country, categoryIds,
            desc, sleeveCond, recordCond, price, stock
        );

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(PRODUCT_ID, result.getId());
        Mockito.verify(pendingNotificationService).enqueueForFollowers(USER_ID, PRODUCT_ID);
        Mockito.verify(notificationService).notifyNewProduct(USER_ID, PRODUCT_ID);
    }

    @Test
    public void testCreateProductWithInvalidPriceThrows() {
        // Arrange & Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, "Title", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.5"), new BigDecimal("9.0"), BigDecimal.ZERO, 1
            );
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, "Title", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.5"), new BigDecimal("9.0"), new BigDecimal("-10.00"), 1
            );
        });
    }

    @Test
    public void testCreateProductWithInvalidConditionThrows() {
        // Arrange & Act & Assert
        // Sleeve condition too high
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, "Title", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("10.5"), new BigDecimal("9.0"), new BigDecimal("100"), 1
            );
        });

        // Record condition too low
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, "Title", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.5"), new BigDecimal("0.5"), new BigDecimal("100"), 1
            );
        });

        // Too many decimal places in sleeve condition
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, "Title", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.555"), new BigDecimal("9.0"), new BigDecimal("100"), 1
            );
        });
    }

    @Test
    public void testCreateProductWithEmptyTitleThrows() {
        // Arrange & Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, "", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.5"), new BigDecimal("9.0"), new BigDecimal("100"), 1
            );
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, null, "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.5"), new BigDecimal("9.0"), new BigDecimal("100"), 1
            );
        });
    }

    @Test
    public void testCreateProductWithInvalidStockThrows() {
        // Arrange & Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(
                USER_ID, "Title", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.5"), new BigDecimal("9.0"), new BigDecimal("100"), 0
            );
        });
    }

    @Test
    public void testUpdateProductAsNonOwnerThrows() {
        // Arrange
        final Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockProduct.getUserId()).thenReturn(999L); // different owner
        Mockito.when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(
                USER_ID, PRODUCT_ID, "Title", "Artist", "Label", "Catalog", "Country", List.of(3L),
                "Description", new BigDecimal("8.5"), new BigDecimal("9.0"), new BigDecimal("100"), 1
            );
        });
    }

    @Test
    public void testUpdateProductSuccess() {
        // Arrange
        final Product mockProduct = Mockito.mock(Product.class);
        Mockito.when(mockProduct.getUserId()).thenReturn(USER_ID);
        Mockito.when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

        final Category category = Mockito.mock(Category.class);
        Mockito.when(category.getId()).thenReturn(3L);
        Mockito.when(categoryService.findByIds(List.of(3L))).thenReturn(List.of(category));

        Mockito.when(productDao.updateProduct(
            Mockito.eq(PRODUCT_ID), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.anyString(),
            Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.any(BigDecimal.class), Mockito.anyInt()
        )).thenReturn(true);

        // Act
        final Product result = productService.updateProduct(
            USER_ID, PRODUCT_ID, "New Title", "New Artist", "New Label", "New Catalog", "New Country", List.of(3L),
            "New Description", new BigDecimal("8.5"), new BigDecimal("9.0"), new BigDecimal("120"), 3
        );

        // Assert
        Assertions.assertNotNull(result);
        Mockito.verify(productDao).updateProduct(
            Mockito.eq(PRODUCT_ID), Mockito.eq("New Title"), Mockito.eq("New Artist"), Mockito.eq("New Label"),
            Mockito.eq("New Catalog"), Mockito.eq("New Country"), Mockito.anyList(), Mockito.eq("New Description"),
            Mockito.eq(new BigDecimal("8.5")), Mockito.eq(new BigDecimal("9.0")), Mockito.eq(new BigDecimal("120")), Mockito.eq(3)
        );
    }

    @Test
    public void testGetProductSearchCriteriaNormalization() {
        // Arrange & Act
        final ProductSearchCriteria criteria = productService.getProductSearchCriteria(
            "   Abbey Road  ", List.of(3L), new BigDecimal("500"), new BigDecimal("100"),
            List.of("Label"), Collections.emptyList(), ProductSortOrder.PRICE_ASC, 1
        );

        // Assert
        Assertions.assertEquals("Abbey Road", criteria.getSearchText());
        Assertions.assertEquals(new BigDecimal("100"), criteria.getMinPrice()); // normalized: swapped
        Assertions.assertEquals(new BigDecimal("500"), criteria.getMaxPrice()); // normalized: swapped
        Assertions.assertEquals(ProductSortOrder.PRICE_ASC, criteria.getSortOrder());
        Assertions.assertEquals(ProductSearchCriteria.DEFAULT_PAGE_SIZE, criteria.getPageSize());
    }

    @Test
    public void testListActiveProductsByUserWithNullUserId() {
        // Arrange & Act
        final PaginatedResult<Product> result = productService.listActiveProductsByUser(null, 1, 10);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getResults().isEmpty());
        Assertions.assertEquals(0, result.getTotalCount());
    }
}
