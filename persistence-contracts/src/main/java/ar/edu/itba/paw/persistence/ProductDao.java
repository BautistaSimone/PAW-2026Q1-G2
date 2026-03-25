package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.List;

import ar.edu.itba.paw.models.Product;

public interface ProductDao {
    Product createProduct(
        final String title,
        final String artist,    // FIXME: id del artista
        final String genre,     // FIXME: id de categoria
        final String vinylCondition,
        final BigDecimal price,
        final String imageUrl, // FIXME: No va a ser una URL sino un id de imagen en la base de datos
        final String description
    );

    List<Product> listProducts();
}
