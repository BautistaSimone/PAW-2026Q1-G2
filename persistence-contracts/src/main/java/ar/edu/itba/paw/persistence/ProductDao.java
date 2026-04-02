package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.List;

import ar.edu.itba.paw.models.Product;

public interface ProductDao {
    Product createProduct(
        final Long userId,
        final String title,
        final String artist,
        final String genre,
        final String description,
        final String condition,
        final BigDecimal price
    );

    List<Product> listProducts();
}
