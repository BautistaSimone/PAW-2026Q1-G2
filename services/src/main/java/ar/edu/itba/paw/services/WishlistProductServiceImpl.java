package ar.edu.itba.paw.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.persistence.WishlistProductDao;

@Service
public class WishlistProductServiceImpl implements WishlistProductService {

    private final WishlistProductDao wishlistProductDao;

    @Autowired
    public WishlistProductServiceImpl(final WishlistProductDao wishlistProductDao) {
        this.wishlistProductDao = wishlistProductDao;
    }

    @Override
    public void createWishlistProduct(
        final Long productId,
        final Long userId
    ) {
        wishlistProductDao.createWishlistProduct(productId, userId);
    }
    @Override
    public List<Product> findByUserId(final Long userId) {
        return wishlistProductDao.findByUserId(userId);
    }
}
