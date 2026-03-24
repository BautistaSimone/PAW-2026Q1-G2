package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.List;

import ar.edu.itba.paw.models.Product;

public interface ProductDao {
    Product createProduct(
        final String title,
        final String artist,
        final String genre,
        final String vinylCondition,
        final BigDecimal price,
        final String imageUrl,
        final String description
    );

    List<Product> listProducts();
}
