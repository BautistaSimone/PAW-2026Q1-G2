package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductState;
import ar.edu.itba.paw.persistence.ProductDao;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;

    @Autowired
    public ProductServiceImpl(final ProductDao productDao) {
        this.productDao = productDao;
    }

    private static String trimToNull(final String s) {
        if (s == null) {
            return null;
        }
        final String t = s.trim();
        return t.isEmpty() ? null : t;
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
        final BigDecimal price
    ) {
        validateProductFields(title, artist, description, sleeveCondition, recordCondition, price);

        return productDao.createProduct(
            userId, trimToNull(title), trimToNull(artist), trimToNull(recordLabel),
            trimToNull(catalogNumber), trimToNull(editionCountry), categoryIds, trimToNull(description),
            sleeveCondition, recordCondition, price
        );
    }

    private static void validateProductFields(
        final String title,
        final String artist,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
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
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
    }

    @Override
    public PaginatedResult<Product> listProducts() {
        return productDao.listProducts();
    }

    @Override
    public PaginatedResult<Product> listProducts(final ProductSearchCriteria criteria) {
        return productDao.findProducts(criteria);
    }

    @Override
    public PaginatedResult<Product> listUserDeletedProducts(final Long userId, final int page, final int pageSize) {
        return productDao.findProductsByUserIdAndState(userId, ProductState.USER_DELETED, page, pageSize);
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
    public boolean hideProductByUser(final Long productId, final Long ownerUserId) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getUserId().equals(ownerUserId)) {
            return false;
        }
        return productDao.markAsUserDeleted(productId);
    }

    @Override
    public void hideProductByAdmin(final Long id) {
        productDao.markAsAdminHidden(id);
    }

    @Override
    public Product updateProduct(
        final Long ownerUserId,
        final Long productId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final List<Long> categoryIds,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price
    ) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getUserId().equals(ownerUserId)) {
            throw new IllegalArgumentException("Not the product owner");
        }
        validateProductFields(title, artist, description, sleeveCondition, recordCondition, price);

        final boolean ok = productDao.updateProduct(
            productId,
            trimToNull(title),
            trimToNull(artist),
            trimToNull(recordLabel),
            trimToNull(catalogNumber),
            trimToNull(editionCountry),
            categoryIds,
            trimToNull(description),
            sleeveCondition,
            recordCondition,
            price
        );
        if (!ok) {
            throw new IllegalStateException("Product cannot be updated (not active or missing)");
        }
        return productDao.findById(productId).orElseThrow(() -> new IllegalStateException("Product missing after update"));
    }

    @Override
    public boolean restoreUserDeletedProduct(final Long productId, final Long ownerUserId) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getUserId().equals(ownerUserId)) {
            return false;
        }
        return productDao.restoreUserDeletedProduct(productId);
    }
}
