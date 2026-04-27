package ar.edu.itba.paw.persistence;

import java.util.List;

import ar.edu.itba.paw.models.Product;

public interface WishlistProductDao {

    void createWishlistProduct(
        final Long productId,
        final Long userId
    );
    
    List<Product> findByUserId(final Long userId);
}
