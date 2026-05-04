package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.ConditionBucket;
import ar.edu.itba.paw.models.ProductSortOrder;
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

    private static String toTitleCase(final String s) {
        final String t = trimToNull(s);
        if (t == null) {
            return null;
        }
        final String[] words = t.split("\\s+");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            final String word = words[i];
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
                if (i < words.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
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
            userId, trimToNull(title), trimToNull(artist), toTitleCase(recordLabel),
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
    public ProductSearchCriteria getProductSearchCriteria(
        final String searchText,
        final List<Long> categoryIds,
        final String minPriceParam,
        final String maxPriceParam,
        final List<String> recordLabels,
        final List<String> estadoParams,
        final String sortParam,
        final int page
    ) {
        BigDecimal minPrice = parsePriceParam(minPriceParam);
		BigDecimal maxPrice = parsePriceParam(maxPriceParam);
		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
			final BigDecimal tmp = minPrice;
			minPrice = maxPrice;
			maxPrice = tmp;
		}

		final List<ConditionBucket> buckets = new ArrayList<>();
		if (estadoParams != null) {
			for (String raw : estadoParams) {
				ConditionBucket.parse(raw).ifPresent(buckets::add);
			}
		}

		final String trimmedSearch = searchText != null ? searchText.trim() : "";
		
		final boolean hasActiveSearch = !trimmedSearch.isEmpty();
		final ProductSortOrder sortOrder = ProductSortOrder.parse(sortParam).orElse(ProductSortOrder.NEWEST);

		return new ProductSearchCriteria(
			hasActiveSearch ? trimmedSearch : null,
			categoryIds,
			minPrice,
			maxPrice,
			recordLabels,
			buckets,
			sortOrder,
			null,
			page,
			12
		);
    }

    @Override
    public List<Product> listProductsNotByUser(final Long userId) {

        List<Product> userProducts = this.listProducts(
            new ProductSearchCriteria(null, null, null, null, null, null, ProductSortOrder.NEWEST, userId, 1, 11)
        ).getResults().stream().limit(10).collect(Collectors.toList());

        return this.listProducts().getResults().stream()
                .filter(p -> userProducts.stream().noneMatch(up -> up.getId().equals(p.getId())))
                .limit(10).collect(Collectors.toList());
    }

    @Override
    public List<Product> listProductsByArtistExcept(final String artist, final Long productId) {
        return this.listProducts(
            new ProductSearchCriteria(artist, null, null, null, null, null, ProductSortOrder.NEWEST, null, 1, 11)
        ).getResults().stream().filter(p -> !p.getId().equals(productId)).limit(10).collect(Collectors.toList());
    }

    @Override
    public List<Product> listProductsByUserExcept(final Long userId, final Long productId) {
        return this.listProducts(
            new ProductSearchCriteria(null, null, null, null, null, null, ProductSortOrder.NEWEST, userId, 1, 11)
        ).getResults().stream().filter(p -> !p.getId().equals(productId)).limit(10).collect(Collectors.toList());
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
    public List<String> listDistinctArtists() {
        return productDao.listDistinctArtists();
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
            toTitleCase(recordLabel),
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

	private static BigDecimal parsePriceParam(final String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String normalized = raw.trim();
		if (normalized.contains(",")) {
			normalized = normalized.replace(".", "").replace(",", ".");
		} else if (normalized.matches("\\d{1,3}(\\.\\d{3})+")) {
			normalized = normalized.replace(".", "");
		}
		if (normalized.endsWith(".")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		try {
			return new BigDecimal(normalized);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
