package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.models.User;

public interface ProductDetailService {

    ProductDetail getProductDetail(Long productId, Long currentUserId);

    final class ProductDetail {

        private final Product product;
        private final boolean isOwnProduct;
        private final boolean isWishlisted;
        private final List<Image> productImages;
        private final SellerRatingSummary sellerRating;
        private final User seller;
        private final List<ar.edu.itba.paw.models.Review> sellerReviews;
        private final List<Product> sellerProducts;
        private final List<Product> relatedProducts;
        private final java.util.Map<Long, SellerRatingSummary> sellerRatings;

        public ProductDetail(
                final Product product,
                final boolean isOwnProduct,
                final boolean isWishlisted,
                final List<Image> productImages,
                final SellerRatingSummary sellerRating,
                final User seller,
                final List<ar.edu.itba.paw.models.Review> sellerReviews,
                final List<Product> sellerProducts,
                final List<Product> relatedProducts,
                final java.util.Map<Long, SellerRatingSummary> sellerRatings) {
            this.product = product;
            this.isOwnProduct = isOwnProduct;
            this.isWishlisted = isWishlisted;
            this.productImages = productImages;
            this.sellerRating = sellerRating;
            this.seller = seller;
            this.sellerReviews = sellerReviews;
            this.sellerProducts = sellerProducts;
            this.relatedProducts = relatedProducts;
            this.sellerRatings = sellerRatings;
        }

        public Product getProduct() {
            return product;
        }

        public boolean isOwnProduct() {
            return isOwnProduct;
        }

        public boolean isWishlisted() {
            return isWishlisted;
        }

        public List<Image> getProductImages() {
            return productImages;
        }

        public SellerRatingSummary getSellerRating() {
            return sellerRating;
        }

        public User getSeller() {
            return seller;
        }

        public List<ar.edu.itba.paw.models.Review> getSellerReviews() {
            return sellerReviews;
        }

        public List<Product> getSellerProducts() {
            return sellerProducts;
        }

        public List<Product> getRelatedProducts() {
            return relatedProducts;
        }

        public java.util.Map<Long, SellerRatingSummary> getSellerRatings() {
            return sellerRatings;
        }
    }
}