package ar.edu.itba.paw.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.models.User;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductService productService;
    private final ImageService imageService;
    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ProductDetailServiceImpl(
            final ProductService productService,
            final ImageService imageService,
            final ReviewService reviewService,
            final UserService userService) {
        this.productService = productService;
        this.imageService = imageService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetail getProductDetail(final Long productId, final Long currentUserId) {
        final Product product = productService.findByIdIfAvailable(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        final boolean isOwnProduct = currentUserId != null && product.getUserId() == currentUserId;
        final boolean isWishlisted = currentUserId != null
                && userService.isProductInWishlist(currentUserId, product.getId());

        final List<Image> productImages = imageService.findAllByProductId(product.getId());

        final SellerRatingSummary sellerRating = reviewService.summaryForSeller(product.getUserId());

        final User seller = userService.findById(product.getUserId())
                .orElse(null);

        final List<ar.edu.itba.paw.models.Review> sellerReviews =
                reviewService.findBySellerId(product.getUserId(), 1, 3).getResults();

        final List<Product> sellerProducts = productService.listProductsByUserExcept(
                product.getUserId(), product.getId());

        final List<Product> relatedProducts = productService.getRelatedProducts(
                product, currentUserId, 10);

        final List<Product> carouselProducts = new ArrayList<>(sellerProducts);
        carouselProducts.addAll(relatedProducts);
        final Map<Long, SellerRatingSummary> sellerRatings =
                reviewService.sellerRatingByProducts(carouselProducts);

        return new ProductDetail(
                product, isOwnProduct, isWishlisted,
                productImages, sellerRating, seller, sellerReviews,
                sellerProducts, relatedProducts, sellerRatings);
    }
}