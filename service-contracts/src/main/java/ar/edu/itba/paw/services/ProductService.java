package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ar.edu.itba.paw.models.ConditionBucket;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductSortOrder;

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
        final BigDecimal price,
        final int stock
    );

    ProductSearchCriteria getProductSearchCriteria(
        final String searchText,
        final List<Long> categoryIds,
        final BigDecimal minPrice,
        final BigDecimal maxPrice,
        final List<String> recordLabels,
        final List<ConditionBucket> estadoParams,
        final ProductSortOrder sortOrder,
        final int page
    );

    PaginatedResult<Product> listProducts();

    PaginatedResult<Product> listProducts(ProductSearchCriteria criteria);

    List<Product> getRecommendedProducts(final Long userId, final int limit, final Long productIdToExclude);

    PaginatedResult<Product> getRecommendedProductsPage(
        final Long userId,
        final int page,
        final int pageSize,
        final Long productIdToExclude
    );

    List<Product> listProductsNotByUser(final Long userId);
    List<Product> listProductsByUserExcept(final Long userId, final Long productId);
    List<Product> listProductsByArtistExcept(final String artist, final Long productId);

    PaginatedResult<Product> listUserDeletedProducts(final Long userId, final int page, final int pageSize);

    PaginatedResult<Product> listActiveProductsByUser(final Long userId, final int page, final int pageSize);

    Map<Long, Long> countActiveProductsByUserIds(final List<Long> userIds);

    Map<Long, List<Product>> listLatestActiveProductsByUserIds(final List<Long> userIds, final int perUserLimit);

    List<String> listDistinctArtists();

    List<String> listDistinctRecordLabels();

    List<String> suggestArtists(final String query, final int limit);

    List<String> suggestRecordLabels(final String query, final int limit);

    Optional<Product> findById(final Long id);

    List<Product> findByIds(java.util.Set<Long> ids);

    Optional<Product> findByIdIfAvailable(final Long id);

    boolean decrementStock(final Long id);

    boolean incrementStock(final Long id);

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
        final BigDecimal price,
        final int stock
    );

    /**
     * Restores a {@code USER_DELETED} listing to {@code ACTIVE} if {@code ownerUserId} owns it.
     * @return {@code true} if restored
     */
    boolean restoreUserDeletedProduct(final Long productId, final Long ownerUserId);
}
