package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;

public interface ProductDao {
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
        final String neighborhood,
        final String province,
        final BigDecimal price
    );

    List<Product> listProducts();

    List<Product> findProducts(ProductSearchCriteria criteria);

    List<String> listDistinctRecordLabels();

    Optional<Product> findById(final Long id);

    Optional<Product> findByIdIfAvailable(final Long id);

    void markAsSold(final Long id);

    void markAsUnavailable(final Long id);
}

