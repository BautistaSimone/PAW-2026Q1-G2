package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.ConditionBucket;
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductState;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.persistence.ImageDao;
import ar.edu.itba.paw.persistence.ProductDao;
import ar.edu.itba.paw.persistence.ReportDao;
import ar.edu.itba.paw.persistence.UserDao;

@Service
public class ProductServiceImpl implements ProductService {

    private static final int MAX_IMAGES_PER_PRODUCT = 8;

    private final ProductDao productDao;
    private final ImageDao imageDao;
    private final ReportDao reportDao;
    private final UserDao userDao;
    private final CategoryService categoryService;
    private final PendingNotificationService pendingNotificationService;
    private final NotificationService notificationService;

    @Autowired
    public ProductServiceImpl(
            final ProductDao productDao,
            final ImageDao imageDao,
            final ReportDao reportDao,
            final UserDao userDao,
            final CategoryService categoryService,
            final PendingNotificationService pendingNotificationService,
            final NotificationService notificationService) {
        this.productDao = productDao;
        this.imageDao = imageDao;
        this.reportDao = reportDao;
        this.userDao = userDao;
        this.categoryService = categoryService;
        this.pendingNotificationService = pendingNotificationService;
        this.notificationService = notificationService;
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
    @Transactional
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
        final BigDecimal price,
        final int stock,
        final List<ProductImageData> images
    ) {
        validatePublisherCanSell(userId);
        validateProductFields(title, artist, description, sleeveCondition, recordCondition, price, stock);
        validateImageDataList(images);

        final List<Category> categories = resolveCategories(categoryIds);

        final Product product = productDao.createProduct(
            userId, trimToNull(title), trimToNull(artist), toTitleCase(recordLabel),
            trimToNull(catalogNumber), trimToNull(editionCountry), categories,
            trimToNull(description), sleeveCondition, recordCondition, price, stock
        );

        persistImages(product.getId(), images);
        pendingNotificationService.enqueueForFollowers(userId, product.getId());
        notificationService.notifyNewProduct(userId, product.getId());

        return product;
    }

    private static void validateProductFields(
        final String title,
        final String artist,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price,
        final int stock
    ) {
        if (stock < 1) {
            throw new IllegalArgumentException("Stock must be at least 1");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be strictly positive");
        }
        if (sleeveCondition == null || sleeveCondition.compareTo(BigDecimal.ONE) < 0 || sleeveCondition.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Sleeve condition must be between 1 and 10");
        }
        if (recordCondition == null || recordCondition.compareTo(BigDecimal.ONE) < 0 || recordCondition.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Record condition must be between 1 and 10");
        }
        requireAtMostTwoFractionDigits(sleeveCondition, "Sleeve condition");
        requireAtMostTwoFractionDigits(recordCondition, "Record condition");
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

    private static void requireAtMostTwoFractionDigits(final BigDecimal value, final String fieldLabel) {
        final int scale = value.stripTrailingZeros().scale();
        if (scale > 2) {
            throw new IllegalArgumentException(fieldLabel + " must have at most two decimal places");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> listProducts() {
        return productDao.listProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSearchCriteria getProductSearchCriteria(
        final String searchText,
        final List<Long> categoryIds,
        final BigDecimal minPrice,
        final BigDecimal maxPrice,
        final List<String> recordLabels,
        final List<ConditionBucket> estadoParams,
        final ProductSortOrder sortOrder,
        final int page
    ) {
        BigDecimal normalizedMin = minPrice;
        BigDecimal normalizedMax = maxPrice;
        if (normalizedMin != null && normalizedMax != null && normalizedMin.compareTo(normalizedMax) > 0) {
            final BigDecimal tmp = normalizedMin;
            normalizedMin = normalizedMax;
            normalizedMax = tmp;
        }

        final List<ConditionBucket> buckets = new ArrayList<>();
        if (estadoParams != null) {
            for (ConditionBucket bucket : estadoParams) {
                if (bucket != null) {
                    buckets.add(bucket);
                }
            }
        }

        final String trimmedSearch = searchText != null ? searchText.trim() : "";
        final boolean hasActiveSearch = !trimmedSearch.isEmpty();
        final ProductSortOrder effectiveSort = sortOrder != null ? sortOrder : ProductSortOrder.NEWEST;

        return new ProductSearchCriteria(
            hasActiveSearch ? trimmedSearch : null,
            categoryIds,
            normalizedMin,
            normalizedMax,
            recordLabels,
            buckets,
            effectiveSort,
            null,
            page,
            ProductSearchCriteria.DEFAULT_PAGE_SIZE
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> listProductsNotByUser(final Long userId) {
        return this.listProducts(
            new ProductSearchCriteria(null, null, null, null, null, null, ProductSortOrder.NEWEST, null, Collections.emptyList(), userId, null, 1, 10)
        ).getResults();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> listProductsByArtistExcept(final String artist, final Long productId) {
        return this.listProducts(
            new ProductSearchCriteria(artist, null, null, null, null, null, ProductSortOrder.NEWEST, null, Collections.emptyList(), null, productId, 1, 10)
        ).getResults();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> listProductsByUserExcept(final Long userId, final Long productId) {
        return this.listProducts(
            new ProductSearchCriteria(null, null, null, null, null, null, ProductSortOrder.NEWEST, userId, Collections.emptyList(), null, productId, 1, 10)
        ).getResults();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> listProducts(final ProductSearchCriteria criteria) {
        return productDao.findProducts(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecommendedProducts(final Long userId, final int limit, final Long productIdToExclude) {
        return productDao.getRecommendedProducts(userId, limit, productIdToExclude);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(final Product product, final Long userId, final int limit) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        List<Product> relatedProducts = new ArrayList<>();
        if (userId != null) {
            relatedProducts = getRecommendedProducts(userId, limit, product.getId());
        }
        if (relatedProducts.isEmpty()) {
            relatedProducts = listProductsByArtistExcept(product.getArtist(), product.getId());
        }
        if (relatedProducts.isEmpty()) {
            relatedProducts = listProductsNotByUser(product.getUserId());
        }
        if (relatedProducts.size() > limit) {
            return relatedProducts.subList(0, limit);
        }
        return relatedProducts;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> getRecommendedProductsPage(
        final Long userId,
        final int page,
        final int pageSize,
        final Long productIdToExclude
    ) {
        return productDao.getRecommendedProductsPage(userId, page, pageSize, productIdToExclude);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> listUserDeletedProducts(final Long userId, final int page, final int pageSize) {
        return productDao.findProductsByUserIdAndState(userId, ProductState.USER_DELETED, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> listActiveProductsByUser(final Long userId, final int page, final int pageSize) {
        if (userId == null) {
            final int safePage = page < 1 ? 1 : page;
            final int safePageSize = pageSize < 1 ? ProductSearchCriteria.DEFAULT_PAGE_SIZE : pageSize;
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }
        return productDao.findActiveProductsByUserId(userId, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Product> productsByPurchaseId(final List<Purchase> purchases) {
        final Map<Long, Product> productsByPurchaseId = new HashMap<>();
        if (purchases == null || purchases.isEmpty()) {
            return productsByPurchaseId;
        }

        final Set<Long> productIds = new HashSet<>();
        for (Purchase purchase : purchases) {
            productIds.add(purchase.getProductId());
        }

        final Map<Long, Product> productsById = new HashMap<>();
        for (Product product : findByIds(productIds)) {
            productsById.put(product.getId(), product);
        }

        for (Purchase purchase : purchases) {
            final Product product = productsById.get(purchase.getProductId());
            if (product != null) {
                productsByPurchaseId.put(purchase.getPurchaseId(), product);
            }
        }
        return productsByPurchaseId;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> listFollowingFeed(final List<Long> followedIds, final int page, final int pageSize) {
        if (followedIds == null || followedIds.isEmpty()) {
            final int safePage = page < 1 ? 1 : page;
            final int safePageSize = pageSize < 1 ? ProductSearchCriteria.DEFAULT_PAGE_SIZE : pageSize;
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }
        final ProductSearchCriteria criteria = new ProductSearchCriteria(
            null,
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            ProductSortOrder.NEWEST,
            null,
            followedIds,
            page,
            pageSize);
        return listProducts(criteria);
    }

    @Transactional(readOnly = true)
    public ProductImageUpdate buildImageUpdate(
            final String layoutRaw,
            final boolean hadExistingImages,
            final List<ProductImageData> newImages) {
        final List<ProductImageData> images = newImages == null ? Collections.emptyList() : newImages;

        if (hadExistingImages && layoutRaw != null && !layoutRaw.isBlank()) {
            final List<ProductImageLayoutParser.Slot> slots = ProductImageLayoutParser.parse(layoutRaw);
            final List<ProductImageUpdate.Entry> entries = new ArrayList<>(slots.size());
            int newImageIndex = 0;
            for (ProductImageLayoutParser.Slot slot : slots) {
                if (slot.getKind() == ProductImageLayoutParser.SlotKind.EXISTING) {
                    entries.add(ProductImageUpdate.existingImage(slot.getExistingImageId()));
                } else {
                    entries.add(ProductImageUpdate.newImage(images.get(newImageIndex++)));
                }
            }
            return ProductImageUpdate.replaceWith(entries);
        }

        if (images.isEmpty()) {
            return ProductImageUpdate.unchanged();
        }
        return ProductImageUpdate.replaceWithNewImages(images);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countActiveProductsByUserIds(final List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return productDao.countActiveProductsByUserIds(userIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<Product>> listLatestActiveProductsByUserIds(final List<Long> userIds, final int perUserLimit) {
        if (userIds == null || userIds.isEmpty() || perUserLimit < 1) {
            return Collections.emptyMap();
        }
        return productDao.findLatestActiveProductsByUserIds(userIds, perUserLimit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listDistinctArtists() {
        return productDao.listDistinctArtists();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listDistinctRecordLabels() {
        return productDao.listDistinctRecordLabels();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> suggestArtists(final String query, final int limit) {
        return productDao.suggestArtists(query, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> suggestRecordLabels(final String query, final int limit) {
        return productDao.suggestRecordLabels(query, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(final Long id) {
        return productDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByIds(java.util.Set<Long> ids) {
        return productDao.findByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findByIdIfAvailable(final Long id) {
        return productDao.findByIdIfAvailable(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findEditableProduct(final Long productId, final Long ownerUserId) {
        if (productId == null || ownerUserId == null) {
            return Optional.empty();
        }
        return productDao.findByIdIfAvailable(productId)
            .filter(product -> ownerUserId.equals(product.getUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canPublishProducts(final Long userId) {
        return userDao.findById(userId)
            .map(user -> user.hasCbuCvu() && user.hasNeighborhoodAndProvince())
            .orElse(false);
    }

    @Override
    @Transactional
    public boolean decrementStock(final Long id) {
        return productDao.decrementStock(id);
    }

    @Override
    @Transactional
    public boolean incrementStock(final Long id) {
        return productDao.incrementStock(id);
    }

    @Override
    @Transactional
    public boolean hideProductByUser(final Long productId, final Long ownerUserId) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getUserId().equals(ownerUserId)) {
            return false;
        }
        return productDao.markAsUserDeleted(productId);
    }

    @Override
    @Transactional
    public void hideProductByAdmin(final Long id) {
        productDao.markAsAdminHidden(id);
        reportDao.deleteByProductId(id);
    }

    @Override
    @Transactional
    public int hideAllProductsByAdmin(final Long userId) {
        final int hidden = productDao.markAllAsAdminHiddenByUserId(userId);
        reportDao.deleteByOwnerUserId(userId);
        return hidden;
    }

    @Override
    @Transactional
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
        final BigDecimal price,
        final int stock,
        final String imageLayout,
        final boolean hadExistingImages,
        final List<ProductImageData> newImages
    ) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getUserId().equals(ownerUserId)) {
            throw new IllegalArgumentException("Not the product owner");
        }
        validatePublisherCanSell(ownerUserId);
        validateProductFields(title, artist, description, sleeveCondition, recordCondition, price, stock);

        final List<Category> categories = resolveCategories(categoryIds);

        final boolean ok = productDao.updateProduct(
            productId,
            trimToNull(title),
            trimToNull(artist),
            toTitleCase(recordLabel),
            trimToNull(catalogNumber),
            trimToNull(editionCountry),
            categories,
            trimToNull(description),
            sleeveCondition,
            recordCondition,
            price,
            stock
        );
        if (!ok) {
            throw new IllegalStateException("Product cannot be updated (not active or missing)");
        }
        final ProductImageUpdate imageUpdate = buildImageUpdate(imageLayout, hadExistingImages, newImages);
        applyImageUpdate(productId, imageUpdate);
        return productDao.findById(productId).orElseThrow(() -> new IllegalStateException("Product missing after update"));
    }

    @Override
    @Transactional
    public boolean restoreUserDeletedProduct(final Long productId, final Long ownerUserId) {
        final Product product = productDao.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getUserId().equals(ownerUserId)) {
            return false;
        }
        return productDao.restoreUserDeletedProduct(productId);
    }

    private List<Category> resolveCategories(final List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> distinctIds = categoryIds.stream()
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        if (distinctIds.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<Long, Category> categoriesById = categoryService.findByIds(distinctIds).stream()
            .collect(Collectors.toMap(Category::getId, category -> category));

        final List<Category> categories = new ArrayList<>();
        for (Long id : distinctIds) {
            final Category category = categoriesById.get(id);
            if (category != null) {
                categories.add(category);
            }
        }
        return categories;
    }

    private void validatePublisherCanSell(final Long userId) {
        final User publisher = userDao.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        if (!publisher.hasCbuCvu() || !publisher.hasNeighborhoodAndProvince()) {
            throw new IllegalStateException("Publisher must complete seller profile data");
        }
    }

    private static void validateImageDataList(final List<ProductImageData> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Product must have at least one image");
        }
        if (images.size() > MAX_IMAGES_PER_PRODUCT) {
            throw new IllegalArgumentException("Too many product images");
        }
        for (ProductImageData image : images) {
            if (image == null) {
                throw new IllegalArgumentException("Image data is required");
            }
        }
    }

    private void persistImages(final Long productId, final List<ProductImageData> images) {
        validateImageDataList(images);
        for (ProductImageData image : images) {
            imageDao.createImage(productId, image.getData(), image.getContentType());
        }
    }

    private void applyImageUpdate(final Long productId, final ProductImageUpdate imageUpdate) {
        final ProductImageUpdate effectiveUpdate = imageUpdate == null ? ProductImageUpdate.unchanged() : imageUpdate;
        if (!effectiveUpdate.isReplace()) {
            return;
        }

        final List<ProductImageData> replacementImages = buildReplacementImages(productId, effectiveUpdate.getEntries());
        imageDao.deleteByProductId(productId);
        persistImages(productId, replacementImages);
    }

    private List<ProductImageData> buildReplacementImages(
            final Long productId,
            final List<ProductImageUpdate.Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Product must have at least one image");
        }
        if (entries.size() > MAX_IMAGES_PER_PRODUCT) {
            throw new IllegalArgumentException("Too many product images");
        }

        final List<ProductImageData> images = new ArrayList<>(entries.size());
        for (ProductImageUpdate.Entry entry : entries) {
            if (entry == null) {
                throw new IllegalArgumentException("Image update entry is required");
            }
            if (entry.getKind() == ProductImageUpdate.EntryKind.EXISTING) {
                final Image image = imageDao.findById(entry.getExistingImageId())
                    .orElseThrow(() -> new IllegalArgumentException("Existing image not found"));
                if (!productId.equals(image.getProductId())) {
                    throw new IllegalArgumentException("Existing image does not belong to product");
                }
                images.add(new ProductImageData(image.getData(), image.getContentType()));
            } else {
                images.add(entry.getImageData());
            }
        }
        validateImageDataList(images);
        return images;
    }
}
