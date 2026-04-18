package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.persistence.ProductDao;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final UserService userService;

    @Autowired
    public ProductServiceImpl(final ProductDao productDao, final UserService userService) {
        this.productDao = productDao;
        this.userService = userService;
    }

    @Override
    public Product createProduct(
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
    ) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be strictly positive");
        }
        if (sleeveCondition == null || sleeveCondition.compareTo(BigDecimal.ONE) < 0 || sleeveCondition.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Sleeve condition must be between 1 and 10");
        }
        if (recordCondition == null || recordCondition.compareTo(BigDecimal.ONE) < 0 || recordCondition.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Record condition must be between 1 and 10");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (artist == null || artist.trim().isEmpty()) {
            throw new IllegalArgumentException("Artist cannot be empty");
        }
        if (neighborhood == null || neighborhood.trim().isEmpty()) {
            throw new IllegalArgumentException("Neighborhood cannot be empty");
        }
        if (province == null || province.trim().isEmpty()) {
            throw new IllegalArgumentException("Province cannot be empty");
        }

        return productDao.createProduct(
            userId, title, artist, recordLabel, catalogNumber, editionCountry, categoryIds, description,
            sleeveCondition, recordCondition, neighborhood, province, price
        );
    }


    @Override
    public List<Product> listProducts() {
        return productDao.listProducts();
    }

    @Override
    public List<Product> listProducts(final ProductSearchCriteria criteria) {
        return productDao.findProducts(criteria);
    }

    @Override
    public List<String> listDistinctRecordLabels() {
        return productDao.listDistinctRecordLabels();
    }

    @Override
    public Optional<Product> findById(final Long id) {
        return productDao.findById(id);
    }

    @Override
    public Optional<Product> findByIdIfAvailable(final Long id) {
        return productDao.findByIdIfAvailable(id);
    }

    @Override
    public boolean reserveIfAvailable(final Long id) {
        return productDao.reserveIfAvailable(id);
    }

    @Override
    public void markAsSold(final Long id) {
        productDao.markAsSold(id);
    }

    @Override
    public void hideProductFromCatalog(final Long id) {
        productDao.markAsUnavailable(id);
    }
}

