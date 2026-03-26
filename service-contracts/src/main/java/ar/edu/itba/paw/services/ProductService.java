package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.io.File;

import ar.edu.itba.paw.models.Product;

public interface ProductService {
    Product createProduct(
        final String title,
        final String artist,
        final String genre,
        final String description,
        final String condition,
        final BigDecimal price
    );

    List<Product> listProducts();
}
