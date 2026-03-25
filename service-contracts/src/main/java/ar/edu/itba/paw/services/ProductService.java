package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;

import ar.edu.itba.paw.models.Product;

public interface ProductService {
    Product createProduct(
        final String title,
        final String artist,
        final String genre,
        final String vinylCondition,
        final BigDecimal price,
        final String imageUrl, // FIXME: No va a ser una URL sino un id de imagen en la base de datos
        final String description
    );

    List<Product> listProducts();
}
