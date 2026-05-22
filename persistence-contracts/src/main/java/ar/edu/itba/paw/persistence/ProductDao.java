package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.List;
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
        final BigDecimal price
    );

    PaginatedResult<Product> listProducts();

    PaginatedResult<Product> findProducts(ProductSearchCriteria criteria);

    List<Product> getRecommendedProducts(final Long userId, final int limit, final Long productIdToExclude);

    PaginatedResult<Product> findProductsByUserIdAndState(
        final Long userId,
        final ProductState state,
        final int page,
        final int pageSize
    );

    List<String> listDistinctArtists();

    List<String> listDistinctRecordLabels();

    List<String> suggestArtists(final String query, final int limit);

    List<String> suggestRecordLabels(final String query, final int limit);

    Optional<Product> findById(final Long id);

    Optional<Product> findByIdIfAvailable(final Long id);

    boolean reserveIfAvailable(final Long id);

    void markAsSold(final Long id);

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
        final BigDecimal price
    );

    boolean restoreUserDeletedProduct(final Long id);
}
