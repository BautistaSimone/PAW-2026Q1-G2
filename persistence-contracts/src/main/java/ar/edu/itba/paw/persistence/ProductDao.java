package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductState;

public interface ProductDao {
    Product createProduct(
        final Long userId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final List<Category> categoryIds,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price,
        final int stock
    );

    PaginatedResult<Product> listProducts();

    PaginatedResult<Product> findProducts(ProductSearchCriteria criteria);

    List<Product> getRecommendedProducts(final Long userId, final int limit, final Long productIdToExclude);

    PaginatedResult<Product> getRecommendedProductsPage(
        final Long userId,
        final int page,
        final int pageSize,
        final Long productIdToExclude
    );

    PaginatedResult<Product> findProductsByUserIdAndState(
        final Long userId,
        final ProductState state,
        final int page,
        final int pageSize
    );

    PaginatedResult<Product> findActiveProductsByUserId(
        final Long userId,
        final int page,
        final int pageSize
    );

    Map<Long, Long> countActiveProductsByUserIds(final List<Long> userIds);

    Map<Long, List<Product>> findLatestActiveProductsByUserIds(
        final List<Long> userIds,
        final int perUserLimit
    );

    List<String> listDistinctArtists();

    List<String> listDistinctRecordLabels();

    List<String> suggestArtists(final String query, final int limit);

    List<String> suggestRecordLabels(final String query, final int limit);

    Optional<Product> findById(final Long id);

    List<Product> findByIds(java.util.Set<Long> ids);

    Optional<Product> findByIdIfAvailable(final Long id);

    boolean decrementStock(final Long id);

    boolean incrementStock(final Long id);

    boolean markAsUserDeleted(final Long id);

    void markAsAdminHidden(final Long id);

    boolean updateProduct(
        final Long productId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final List<Category> categories,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price,
        final int stock
    );

    boolean restoreUserDeletedProduct(final Long id);

    /**
     * Marks ALL active products of a given user as ADMIN_HIDDEN in a single UPDATE.
     * @param userId the owner whose products should be hidden
     * @return the number of products affected
     */
    int markAllAsAdminHiddenByUserId(final Long userId);
}
