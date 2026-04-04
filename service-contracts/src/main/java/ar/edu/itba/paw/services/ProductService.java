package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;

public interface ProductService {
    Product createProduct(
        final String sellerEmail,
        final String title,
        final String artist,
        final String recordLabel,
        final List<Long> categoryIds,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final String neighborhood,
        final String province,
        final BigDecimal price
    );

    List<Product> listProducts();

    List<Product> listProducts(ProductSearchCriteria criteria);

    List<String> listDistinctRecordLabels();

    Optional<Product> findById(final Long id);

    void markAsSold(final Long id);
}

