package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.persistence.ProductDao;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;

    @Autowired
    public ProductServiceImpl(final ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Product createProduct(
        final String title,
        final String artist,
        final String genre,
        final String description,
        final String condition,
        final BigDecimal price
    ) {
        return productDao.createProduct(title, artist, genre, description, condition, price);
    }

    @Override
    public List<Product> listProducts() {
        return productDao.listProducts();
    }
}
