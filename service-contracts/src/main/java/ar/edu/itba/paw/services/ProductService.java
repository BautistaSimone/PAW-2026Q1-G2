package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;

public interface ProductService {
    Product createProduct(
        final Long userId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final List<Long> categoryIds,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price
    );

    PaginatedResult<Product> listProducts();

    PaginatedResult<Product> listProducts(ProductSearchCriteria criteria);

    PaginatedResult<Product> listUserDeletedProducts(final Long userId, final int page, final int pageSize);

    List<String> listDistinctArtists();

    List<String> listDistinctRecordLabels();

    Optional<Product> findById(final Long id);

    Optional<Product> findByIdIfAvailable(final Long id);

    boolean reserveIfAvailable(final Long id);

    void markAsSold(final Long id);

    /**
     * Marks an {@code ACTIVE} listing as deleted by the owner (restorable).
     * @return {@code true} if the row was updated
     */
    boolean hideProductByUser(final Long productId, final Long ownerUserId);

    /** Hides a product from the catalog (admin / moderation); any previous state is overwritten. */
    void hideProductByAdmin(final Long id);

    Product updateProduct(
        final Long ownerUserId,
        final Long productId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final List<Long> categoryIds,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price
    );

    /**
     * Restores a {@code USER_DELETED} listing to {@code ACTIVE} if {@code ownerUserId} owns it.
     * @return {@code true} if restored
     */
    boolean restoreUserDeletedProduct(final Long productId, final Long ownerUserId);
}
