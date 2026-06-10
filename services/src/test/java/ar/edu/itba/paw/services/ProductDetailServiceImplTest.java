package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.models.User;

@ExtendWith(MockitoExtension.class)
public class ProductDetailServiceImplTest {

    private static final long PRODUCT_ID = 10L;
    private static final long SELLER_ID = 1L;
    private static final long CURRENT_USER_ID = 2L;
    private static final long IMAGE_ID = 100L;

    @Mock
    private ProductService productService;

    @Mock
    private ImageService imageService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    private ProductDetailServiceImpl productDetailService;

    @BeforeEach
    public void setUp() {
        productDetailService = new ProductDetailServiceImpl(
                productService,
                imageService,
                reviewService,
                userService);
    }

    @Test
    public void getProductDetailReturnsDetailForAuthenticatedUser() {
        final Product product = new Product(PRODUCT_ID, SELLER_ID, "Album", "Artist", "Label", "CAT", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        final Image image = new Image(IMAGE_ID, PRODUCT_ID, new byte[]{1, 2, 3}, "image/jpeg");
        final User seller = new User(SELLER_ID, "seller@test.com", "pass", "seller", false, true, false,
                "John", "Doe", "Main", "123", "Palermo", "CABA", null, null);
        final Review review = new Review(1L, 99L, SELLER_ID, 3L, 5, "Great seller", LocalDateTime.now(), "buyer1");
        final PaginatedResult<Review> reviewPage = new PaginatedResult<>(List.of(review), 1, 3, 1);
        final Product sellerProduct = new Product(11L, SELLER_ID, "Other", "Artist", "Label", "CAT2", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(50), 1);
        final Product relatedProduct = new Product(12L, 4L, "Related", "OtherArtist", "Label2", "CAT3", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(80), 1);
        final SellerRatingSummary ratingSummary = new SellerRatingSummary(4.5, 10);
        final Map<Long, SellerRatingSummary> sellerRatings = Map.of(SELLER_ID, ratingSummary);

        Mockito.when(productService.findByIdIfAvailable(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(userService.isProductInWishlist(CURRENT_USER_ID, PRODUCT_ID)).thenReturn(true);
        Mockito.when(imageService.findAllByProductId(PRODUCT_ID)).thenReturn(List.of(image));
        Mockito.when(reviewService.summaryForSeller(SELLER_ID)).thenReturn(ratingSummary);
        Mockito.when(userService.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        Mockito.when(reviewService.findBySellerId(SELLER_ID, 1, 3)).thenReturn(reviewPage);
        Mockito.when(productService.listProductsByUserExcept(SELLER_ID, PRODUCT_ID)).thenReturn(List.of(sellerProduct));
        Mockito.when(productService.getRelatedProducts(product, CURRENT_USER_ID, 10)).thenReturn(List.of(relatedProduct));
        Mockito.when(reviewService.sellerRatingByProducts(List.of(sellerProduct, relatedProduct))).thenReturn(sellerRatings);

        final ProductDetailService.ProductDetail result = productDetailService.getProductDetail(PRODUCT_ID, CURRENT_USER_ID);

        Assertions.assertSame(product, result.getProduct());
        Assertions.assertFalse(result.isOwnProduct());
        Assertions.assertTrue(result.isWishlisted());
        Assertions.assertEquals(1, result.getProductImages().size());
        Assertions.assertSame(image, result.getProductImages().get(0));
        Assertions.assertSame(ratingSummary, result.getSellerRating());
        Assertions.assertSame(seller, result.getSeller());
        Assertions.assertEquals(1, result.getSellerReviews().size());
        Assertions.assertSame(review, result.getSellerReviews().get(0));
        Assertions.assertEquals(1, result.getSellerProducts().size());
        Assertions.assertEquals(1, result.getRelatedProducts().size());
        Assertions.assertSame(sellerRatings, result.getSellerRatings());
    }

    @Test
    public void getProductDetailReturnsDetailForUnauthenticatedUser() {
        final Product product = new Product(PRODUCT_ID, SELLER_ID, "Album", "Artist", "Label", "CAT", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);
        final SellerRatingSummary ratingSummary = new SellerRatingSummary(4.0, 5);
        final User seller = new User(SELLER_ID, "seller@test.com", "pass", "seller", false, true, false,
                "John", "Doe", "Main", "123", "Palermo", "CABA", null, null);
        final Product sellerProduct = new Product(11L, SELLER_ID, "Other", "Artist", "Label", "CAT2", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(50), 1);

        Mockito.when(productService.findByIdIfAvailable(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(imageService.findAllByProductId(PRODUCT_ID)).thenReturn(Collections.emptyList());
        Mockito.when(reviewService.summaryForSeller(SELLER_ID)).thenReturn(ratingSummary);
        Mockito.when(userService.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        Mockito.when(reviewService.findBySellerId(SELLER_ID, 1, 3)).thenReturn(new PaginatedResult<>(Collections.emptyList(), 1, 3, 0));
        Mockito.when(productService.listProductsByUserExcept(SELLER_ID, PRODUCT_ID)).thenReturn(List.of(sellerProduct));
        Mockito.when(productService.getRelatedProducts(product, null, 10)).thenReturn(Collections.emptyList());
        Mockito.when(reviewService.sellerRatingByProducts(List.of(sellerProduct))).thenReturn(Collections.emptyMap());

        final ProductDetailService.ProductDetail result = productDetailService.getProductDetail(PRODUCT_ID, null);

        Assertions.assertSame(product, result.getProduct());
        Assertions.assertFalse(result.isOwnProduct());
        Assertions.assertFalse(result.isWishlisted());
        Assertions.assertTrue(result.getProductImages().isEmpty());
        Assertions.assertSame(seller, result.getSeller());
    }

    @Test
    public void getProductDetailReturnsOwnProductFlagsCorrectly() {
        final Product product = new Product(PRODUCT_ID, CURRENT_USER_ID, "Album", "Artist", "Label", "CAT", "Country",
                Collections.emptyList(), "Desc", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), BigDecimal.valueOf(100), 1);

        Mockito.when(productService.findByIdIfAvailable(PRODUCT_ID)).thenReturn(Optional.of(product));
        Mockito.when(imageService.findAllByProductId(PRODUCT_ID)).thenReturn(Collections.emptyList());
        Mockito.when(reviewService.summaryForSeller(CURRENT_USER_ID)).thenReturn(new SellerRatingSummary(0, 0));
        Mockito.when(reviewService.findBySellerId(CURRENT_USER_ID, 1, 3)).thenReturn(new PaginatedResult<>(Collections.emptyList(), 1, 3, 0));
        Mockito.when(productService.listProductsByUserExcept(CURRENT_USER_ID, PRODUCT_ID)).thenReturn(Collections.emptyList());
        Mockito.when(productService.getRelatedProducts(product, CURRENT_USER_ID, 10)).thenReturn(Collections.emptyList());
        Mockito.when(reviewService.sellerRatingByProducts(Collections.emptyList())).thenReturn(Collections.emptyMap());

        final ProductDetailService.ProductDetail result = productDetailService.getProductDetail(PRODUCT_ID, CURRENT_USER_ID);

        Assertions.assertTrue(result.isOwnProduct());
        Assertions.assertFalse(result.isWishlisted());
    }

    @Test
    public void getProductDetailThrowsWhenProductNotFound() {
        Mockito.when(productService.findByIdIfAvailable(PRODUCT_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> productDetailService.getProductDetail(PRODUCT_ID, CURRENT_USER_ID));
    }
}