package ar.edu.itba.paw.services;

import java.util.List;

import ar.edu.itba.paw.models.Product;

public interface WishlistProductService {

    void createWishlistProduct(
        final Long productId,
        final Long userId
    );

    List<Product> findByUserId(final Long userId);
}
