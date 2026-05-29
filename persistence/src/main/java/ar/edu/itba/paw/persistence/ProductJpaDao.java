package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.ConditionBucket;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductState;

@Repository
public class ProductJpaDao implements ProductDao {

    private static final int MIN_SUGGESTION_QUERY_LENGTH = 2;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Product> findById(final Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    @Override
    public Product createProduct(
        final Long userId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final List<Category> categories,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price
    ) {
        final LocalDate published = LocalDate.now();

        final Product product = new Product(
            userId,
            title,
            artist,
            normalizeRecordLabel(recordLabel),
            normalizeRecordLabel(catalogNumber),
            normalizeRecordLabel(editionCountry),
            categories,
            description,
            sleeveCondition,
            recordCondition,
            published,
            price
        );

        em.persist(product);
        return product;
    }

    @Override
    public PaginatedResult<Product> listProducts() {
        return findProducts(ProductSearchCriteria.empty());
    }

    @Override
    public PaginatedResult<Product> findProducts(final ProductSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        List<Predicate> conditions = new ArrayList<>();

        // Product must be active
        conditions.add(cb.equal(root.get("state"), ProductState.ACTIVE.getPersistenceValue()));

        if (criteria.getMinPrice() != null) {
            conditions.add(cb.ge(root.get("price"), criteria.getMinPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            conditions.add(cb.le(root.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getSearchText() != null && !criteria.getSearchText().isBlank()) {
            final String raw = criteria.getSearchText().trim().toLowerCase();
            final boolean needsEscape = raw.contains("%") || raw.contains("_") || raw.contains("\\");
            final String needle = "%" + (needsEscape ? escapeForLike(raw) : raw) + "%";
            javax.persistence.criteria.Expression<String> pattern = cb.literal(needle);
            Predicate p1 = needsEscape
                ? cb.like(cb.lower(root.get("title")), pattern, '\\')
                : cb.like(cb.lower(root.get("title")), pattern);
            Predicate p2 = needsEscape
                ? cb.like(cb.lower(root.get("artist")), pattern, '\\')
                : cb.like(cb.lower(root.get("artist")), pattern);
            Predicate p3 = needsEscape
                ? cb.like(cb.lower(root.get("description")), pattern, '\\')
                : cb.like(cb.lower(root.get("description")), pattern);
            conditions.add(cb.or(p1, p2, p3));
        }

        if (!criteria.getCategoryIds().isEmpty()) {
            javax.persistence.criteria.Join<Product, Category> categoriesJoin = root.join("categories");
            conditions.add(categoriesJoin.get("id").in(criteria.getCategoryIds()));
        }

        if (!criteria.getRecordLabels().isEmpty()) {
            conditions.add(root.get("recordLabel").in(criteria.getRecordLabels()));
        }

        if (!criteria.getConditionBuckets().isEmpty()) {
            List<Predicate> bucketPredicates = new ArrayList<>();
            javax.persistence.criteria.Expression<Number> avgCond = cb.quot(cb.sum(root.get("sleeveCondition"), root.get("recordCondition")), 2.0);
            
            for (ConditionBucket bucket : criteria.getConditionBuckets()) {
                switch (bucket) {
                    case EXCELENTE -> bucketPredicates.add(cb.ge(avgCond, 9.0));
                    case MUY_BUENO -> bucketPredicates.add(cb.and(cb.ge(avgCond, 8.0), cb.lt(avgCond, 9.0)));
                    case BUENO -> bucketPredicates.add(cb.and(cb.ge(avgCond, 7.0), cb.lt(avgCond, 8.0)));
                    case REGULAR -> bucketPredicates.add(cb.lt(avgCond, 7.0));
                }
            }
            conditions.add(cb.or(bucketPredicates.toArray(new Predicate[0])));
        }

        if (criteria.getUserId() != null) {
            conditions.add(cb.equal(root.get("userId"), criteria.getUserId()));
        }

        if (!criteria.getUserIds().isEmpty()) {
            conditions.add(root.get("userId").in(criteria.getUserIds()));
        }

        // Fetches the count of all Products as per given criteria
        Predicate[] predicates = conditions.toArray(new Predicate[0]);
        
        // Create Count Query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> productRootCount = countQuery.from(Product.class);
        
        // We need to recreate the joins for the count query if they exist
        List<Predicate> countConditions = new ArrayList<>();
        countConditions.add(cb.equal(productRootCount.get("state"), ProductState.ACTIVE.getPersistenceValue()));
        if (criteria.getMinPrice() != null) countConditions.add(cb.ge(productRootCount.get("price"), criteria.getMinPrice()));
        if (criteria.getMaxPrice() != null) countConditions.add(cb.le(productRootCount.get("price"), criteria.getMaxPrice()));
        if (criteria.getSearchText() != null && !criteria.getSearchText().isBlank()) {
            final String raw = criteria.getSearchText().trim().toLowerCase();
            final boolean needsEscape = raw.contains("%") || raw.contains("_") || raw.contains("\\");
            final String needle = "%" + (needsEscape ? escapeForLike(raw) : raw) + "%";
            javax.persistence.criteria.Expression<String> pattern = cb.literal(needle);
            Predicate p1 = needsEscape
                ? cb.like(cb.lower(productRootCount.get("title")), pattern, '\\')
                : cb.like(cb.lower(productRootCount.get("title")), pattern);
            Predicate p2 = needsEscape
                ? cb.like(cb.lower(productRootCount.get("artist")), pattern, '\\')
                : cb.like(cb.lower(productRootCount.get("artist")), pattern);
            Predicate p3 = needsEscape
                ? cb.like(cb.lower(productRootCount.get("description")), pattern, '\\')
                : cb.like(cb.lower(productRootCount.get("description")), pattern);
            countConditions.add(cb.or(p1, p2, p3));
        }
        if (!criteria.getCategoryIds().isEmpty()) {
            javax.persistence.criteria.Join<Product, Category> countCategoriesJoin = productRootCount.join("categories");
            countConditions.add(countCategoriesJoin.get("id").in(criteria.getCategoryIds()));
        }
        if (!criteria.getRecordLabels().isEmpty()) countConditions.add(productRootCount.get("recordLabel").in(criteria.getRecordLabels()));
        if (!criteria.getConditionBuckets().isEmpty()) {
            List<Predicate> bucketPredicates = new ArrayList<>();
            javax.persistence.criteria.Expression<Number> avgCond = cb.quot(cb.sum(productRootCount.get("sleeveCondition"), productRootCount.get("recordCondition")), 2.0);
            for (ConditionBucket bucket : criteria.getConditionBuckets()) {
                switch (bucket) {
                    case EXCELENTE -> bucketPredicates.add(cb.ge(avgCond, 9.0));
                    case MUY_BUENO -> bucketPredicates.add(cb.and(cb.ge(avgCond, 8.0), cb.lt(avgCond, 9.0)));
                    case BUENO -> bucketPredicates.add(cb.and(cb.ge(avgCond, 7.0), cb.lt(avgCond, 8.0)));
                    case REGULAR -> bucketPredicates.add(cb.lt(avgCond, 7.0));
                }
            }
            countConditions.add(cb.or(bucketPredicates.toArray(new Predicate[0])));
        }
        if (criteria.getUserId() != null) countConditions.add(cb.equal(productRootCount.get("userId"), criteria.getUserId()));
        if (!criteria.getUserIds().isEmpty()) countConditions.add(productRootCount.get("userId").in(criteria.getUserIds()));

        countQuery.select(cb.countDistinct(productRootCount)).where(cb.and(countConditions.toArray(new Predicate[0])));
        Long count = em.createQuery(countQuery).getSingleResult();

        if (count == 0) {
            return new PaginatedResult<>(Collections.emptyList(), criteria.getPage(), criteria.getPageSize(), 0);
        }

        query.select(root).distinct(true).where(cb.and(predicates));

        List<javax.persistence.criteria.Order> orders = new ArrayList<>();
        switch(criteria.getSortOrder()) {
            case NEWEST -> {
                orders.add(cb.desc(root.get("published"))); 
                orders.add(cb.desc(root.get("productId")));
            }
            case OLDEST -> {
                orders.add(cb.asc(root.get("published"))); 
                orders.add(cb.asc(root.get("productId")));
            }
            case PRICE_ASC -> orders.add(cb.asc(root.get("price")));
            case PRICE_DESC -> orders.add(cb.desc(root.get("price")));
            case NAME_ASC -> orders.add(cb.asc(cb.lower(root.get("title"))));
            case NAME_DESC -> orders.add(cb.desc(cb.lower(root.get("title"))));
            case CONDITION_DESC -> orders.add(cb.desc(cb.quot(cb.sum(root.get("sleeveCondition"), root.get("recordCondition")), 2.0)));
            case CONDITION_ASC -> orders.add(cb.asc(cb.quot(cb.sum(root.get("sleeveCondition"), root.get("recordCondition")), 2.0)));
        }
        query.orderBy(orders);

        // This query fetches the Products as per the Page Limit
        List<Product> result = em.createQuery(query)
            .setFirstResult((criteria.getPage() - 1) * criteria.getPageSize())
            .setMaxResults(criteria.getPageSize())
            .getResultList();

        return new PaginatedResult<>(result, criteria.getPage(), criteria.getPageSize(), count);
    }

    @Override
    public List<Product> getRecommendedProducts(final Long userId, final int limit, final Long productIdToExclude) {
        if (userId == null || limit < 1) {
            return Collections.emptyList();
        }

        final String baseSql = "WITH fav_cats AS (" +
            " SELECT category_id FROM user_favorite_categories WHERE user_id = :userId" +
            "), wishlist_cats AS (" +
            " SELECT DISTINCT pc.category_id" +
            " FROM user_wishlist_products uwp" +
            " JOIN products_categories pc ON pc.product_id = uwp.product_id" +
            " WHERE uwp.user_id = :userId" +
            "), purchase_cats AS (" +
            " SELECT DISTINCT pc.category_id" +
            " FROM purchases pu" +
            " JOIN products_categories pc ON pc.product_id = pu.product_id" +
            " WHERE pu.buyer_user_id = :userId" +
            "), fav_match AS (" +
            " SELECT pc.product_id, COUNT(DISTINCT pc.category_id) AS match_count" +
            " FROM products_categories pc" +
            " JOIN fav_cats fc ON fc.category_id = pc.category_id" +
            " GROUP BY pc.product_id" +
            "), wishlist_match AS (" +
            " SELECT pc.product_id, COUNT(DISTINCT pc.category_id) AS match_count" +
            " FROM products_categories pc" +
            " JOIN wishlist_cats wc ON wc.category_id = pc.category_id" +
            " GROUP BY pc.product_id" +
            "), purchase_match AS (" +
            " SELECT pc.product_id, COUNT(DISTINCT pc.category_id) AS match_count" +
            " FROM products_categories pc" +
            " JOIN purchase_cats pcats ON pcats.category_id = pc.category_id" +
            " GROUP BY pc.product_id" +
            ")" +
            " SELECT p.*" +
            " FROM products p" +
            " LEFT JOIN fav_match fm ON fm.product_id = p.product_id" +
            " LEFT JOIN wishlist_match wm ON wm.product_id = p.product_id" +
            " LEFT JOIN purchase_match pm ON pm.product_id = p.product_id" +
            " WHERE p.state = :state" +
            " AND p.user_id <> :userId";

        final String sql = productIdToExclude == null
            ? baseSql +
                " ORDER BY" +
                " COALESCE(fm.match_count, 0) DESC," +
                " COALESCE(wm.match_count, 0) DESC," +
                " COALESCE(pm.match_count, 0) DESC," +
                " p.published DESC, p.product_id DESC"
            : baseSql +
                " AND p.product_id <> :excludeId" +
                " ORDER BY" +
                " COALESCE(fm.match_count, 0) DESC," +
                " COALESCE(wm.match_count, 0) DESC," +
                " COALESCE(pm.match_count, 0) DESC," +
                " p.published DESC, p.product_id DESC";

        final javax.persistence.Query query = em.createNativeQuery(sql, Product.class)
            .setParameter("userId", userId)
            .setParameter("state", ProductState.ACTIVE.getPersistenceValue());

        if (productIdToExclude != null) {
            query.setParameter("excludeId", productIdToExclude);
        }

        @SuppressWarnings("unchecked")
        final List<Product> results = query
            .setMaxResults(limit)
            .getResultList();

        return results;
    }

    @Override
    public PaginatedResult<Product> getRecommendedProductsPage(
        final Long userId,
        final int page,
        final int pageSize,
        final Long productIdToExclude
    ) {
        final int safePage = page < 1 ? 1 : page;
        final int safePageSize = pageSize < 1 ? 12 : pageSize;

        if (userId == null) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final String baseSql = "WITH fav_cats AS (" +
            " SELECT category_id FROM user_favorite_categories WHERE user_id = :userId" +
            "), wishlist_cats AS (" +
            " SELECT DISTINCT pc.category_id" +
            " FROM user_wishlist_products uwp" +
            " JOIN products_categories pc ON pc.product_id = uwp.product_id" +
            " WHERE uwp.user_id = :userId" +
            "), purchase_cats AS (" +
            " SELECT DISTINCT pc.category_id" +
            " FROM purchases pu" +
            " JOIN products_categories pc ON pc.product_id = pu.product_id" +
            " WHERE pu.buyer_user_id = :userId" +
            "), fav_match AS (" +
            " SELECT pc.product_id, COUNT(DISTINCT pc.category_id) AS match_count" +
            " FROM products_categories pc" +
            " JOIN fav_cats fc ON fc.category_id = pc.category_id" +
            " GROUP BY pc.product_id" +
            "), wishlist_match AS (" +
            " SELECT pc.product_id, COUNT(DISTINCT pc.category_id) AS match_count" +
            " FROM products_categories pc" +
            " JOIN wishlist_cats wc ON wc.category_id = pc.category_id" +
            " GROUP BY pc.product_id" +
            "), purchase_match AS (" +
            " SELECT pc.product_id, COUNT(DISTINCT pc.category_id) AS match_count" +
            " FROM products_categories pc" +
            " JOIN purchase_cats pcats ON pcats.category_id = pc.category_id" +
            " GROUP BY pc.product_id" +
            ")";

        final String whereSql = productIdToExclude == null
            ? " WHERE p.state = :state AND p.user_id <> :userId"
            : " WHERE p.state = :state AND p.user_id <> :userId AND p.product_id <> :excludeId";

        final String orderSql = " ORDER BY" +
            " COALESCE(fm.match_count, 0) DESC," +
            " COALESCE(wm.match_count, 0) DESC," +
            " COALESCE(pm.match_count, 0) DESC," +
            " p.published DESC, p.product_id DESC";

        final String countSql = baseSql +
            " SELECT COUNT(*)" +
            " FROM products p" +
            " LEFT JOIN fav_match fm ON fm.product_id = p.product_id" +
            " LEFT JOIN wishlist_match wm ON wm.product_id = p.product_id" +
            " LEFT JOIN purchase_match pm ON pm.product_id = p.product_id" +
            whereSql;

        final javax.persistence.Query countQuery = em.createNativeQuery(countSql)
            .setParameter("userId", userId)
            .setParameter("state", ProductState.ACTIVE.getPersistenceValue());

        if (productIdToExclude != null) {
            countQuery.setParameter("excludeId", productIdToExclude);
        }

        final Number countResult = (Number) countQuery.getSingleResult();
        final int total = countResult == null ? 0 : countResult.intValue();
        if (total == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final String selectSql = baseSql +
            " SELECT p.*" +
            " FROM products p" +
            " LEFT JOIN fav_match fm ON fm.product_id = p.product_id" +
            " LEFT JOIN wishlist_match wm ON wm.product_id = p.product_id" +
            " LEFT JOIN purchase_match pm ON pm.product_id = p.product_id" +
            whereSql +
            orderSql;

        final javax.persistence.Query query = em.createNativeQuery(selectSql, Product.class)
            .setParameter("userId", userId)
            .setParameter("state", ProductState.ACTIVE.getPersistenceValue());

        if (productIdToExclude != null) {
            query.setParameter("excludeId", productIdToExclude);
        }

        @SuppressWarnings("unchecked")
        final List<Product> results = query
            .setFirstResult((safePage - 1) * safePageSize)
            .setMaxResults(safePageSize)
            .getResultList();

        return new PaginatedResult<>(results, safePage, safePageSize, total);
    }

    @Override
    public PaginatedResult<Product> findProductsByUserIdAndState(
        final Long userId,
        final ProductState state,
        final int page,
        final int pageSize
    ) {
        final int safePage = page < 1 ? 1 : page;
        final int safePageSize = pageSize < 1 ? 12 : pageSize;
        final String stateVal = state.getPersistenceValue();

        // Paginate with 1 + 1 queries
        @SuppressWarnings("unchecked")
        List<Number> ids = em.createNativeQuery("SELECT product_id FROM products WHERE user_id = :userId AND state = :state")
            .setParameter("userId", userId)
            .setParameter("state", stateVal)
            .setFirstResult((safePage-1) * safePageSize)
            .setMaxResults(safePageSize)
            .getResultList();

        if (ids.isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final TypedQuery<Product> selectQuery = em.createQuery("FROM Product WHERE productId IN :ids", Product.class)
            .setParameter("ids", ids.stream().map(Number::longValue).collect(Collectors.toList()));

        return new PaginatedResult<>(selectQuery.getResultList(), safePage, safePageSize, ids.size());
    }

    @Override
    public List<String> listDistinctArtists() {
        return em.createQuery(
            "SELECT DISTINCT TRIM(p.artist) FROM Product p " +
            "WHERE p.artist IS NOT NULL AND p.state = :state AND TRIM(p.artist) <> '' " +
            "ORDER BY TRIM(p.artist) ASC", String.class
        ).setParameter("state", ProductState.ACTIVE.getPersistenceValue())
        .getResultList();
    }

    @Override
    public List<String> listDistinctRecordLabels() {
        return em.createQuery(
            "SELECT DISTINCT TRIM(p.recordLabel) FROM Product p " +
            "WHERE p.recordLabel IS NOT NULL AND p.state = :state AND TRIM(p.recordLabel) <> '' " +
            "ORDER BY TRIM(p.recordLabel) ASC", String.class
        ).setParameter("state", ProductState.ACTIVE.getPersistenceValue())
        .getResultList();
    }

    @Override
    public List<String> suggestArtists(final String query, final int limit) {
        return suggestDistinctField("artist", query, limit);
    }

    @Override
    public List<String> suggestRecordLabels(final String query, final int limit) {
        return suggestDistinctField("recordLabel", query, limit);
    }

    private List<String> suggestDistinctField(final String fieldName, final String rawQuery, final int limit) {
        final String query = normalizeSuggestionQuery(rawQuery);
        if (query.length() < MIN_SUGGESTION_QUERY_LENGTH || limit < 1) {
            return Collections.emptyList();
        }

        final String trimmedField = "TRIM(p." + fieldName + ")";
        final String normalizedField = "LOWER(" + trimmedField + ")";
        final String escapedQuery = escapeForLike(query);

        final TypedQuery<Object[]> suggestionsQuery = em.createQuery(
            "SELECT DISTINCT " +
            trimmedField + " AS suggestion, " +
            "CASE WHEN " + normalizedField + " = :query THEN 0 ELSE 1 END AS exactRank, " +
            "CASE WHEN " + normalizedField + " LIKE :prefix ESCAPE '\\' THEN 0 ELSE 1 END AS prefixRank, " +
            "LOCATE(:query, " + normalizedField + ") AS matchPosition, " +
            "LENGTH(" + trimmedField + ") AS suggestionLength, " +
            normalizedField + " AS normalizedSuggestion " +
            "FROM Product p " +
            "WHERE p." + fieldName + " IS NOT NULL " +
            "AND p.state = :state " +
            "AND " + trimmedField + " <> '' " +
            "AND " + normalizedField + " LIKE :needle ESCAPE '\\' " +
            "ORDER BY " +
            "exactRank ASC, " +
            "prefixRank ASC, " +
            "matchPosition ASC, " +
            "suggestionLength ASC, " +
            "normalizedSuggestion ASC",
            Object[].class
        );

        return suggestionsQuery
            .setParameter("state", ProductState.ACTIVE.getPersistenceValue())
            .setParameter("query", query)
            .setParameter("needle", "%" + escapedQuery + "%")
            .setParameter("prefix", escapedQuery + "%")
            .setMaxResults(limit)
            .getResultList()
            .stream()
            .map(row -> (String) row[0])
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findByIdIfAvailable(final Long id) {
        final TypedQuery<Product> query = em.createQuery(
            "FROM Product WHERE productId = :productId AND state = :state", Product.class
        );
        query.setParameter("productId", id);
        query.setParameter("state", ProductState.ACTIVE.getPersistenceValue());
        return query.getResultList().stream().findFirst();
    }

    @Override
    public boolean reserveIfAvailable(final Long id) {
        return em.createQuery(
            "UPDATE Product SET state = :newState WHERE productId = :productId AND state = :currentState")
            .setParameter("newState", ProductState.RESERVED.getPersistenceValue())
            .setParameter("productId", id)
            .setParameter("currentState", ProductState.ACTIVE.getPersistenceValue())
            .executeUpdate() >= 1;
    }

    @Override
    public boolean releaseReservation(final Long id) {
        return em.createQuery(
            "UPDATE Product SET state = :newState WHERE productId = :productId AND state = :currentState")
            .setParameter("newState", ProductState.ACTIVE.getPersistenceValue())
            .setParameter("productId", id)
            .setParameter("currentState", ProductState.RESERVED.getPersistenceValue())
            .executeUpdate() >= 1;
    }

    @Override
    public void markAsSold(final Long id) {
        em.createQuery(
            "UPDATE Product SET state = :newState WHERE productId = :productId AND state = :currentState")
            .setParameter("newState", ProductState.SOLD.getPersistenceValue())
            .setParameter("productId", id)
            .setParameter("currentState", ProductState.RESERVED.getPersistenceValue())
            .executeUpdate();
    }

    @Override
    public boolean markAsUserDeleted(final Long id) {
        return em.createQuery(
            "UPDATE Product SET state = :newState WHERE productId = :productId AND state = :currentState")
            .setParameter("newState", ProductState.USER_DELETED.getPersistenceValue())
            .setParameter("productId", id)
            .setParameter("currentState", ProductState.ACTIVE.getPersistenceValue())
            .executeUpdate() >= 1;
    }

    @Override
    public void markAsAdminHidden(final Long id) {
        em.createQuery(
            "UPDATE Product SET state = :newState WHERE productId = :productId")
            .setParameter("newState", ProductState.ADMIN_HIDDEN.getPersistenceValue())
            .setParameter("productId", id)
            .executeUpdate();
    }

    @Override
    public boolean updateProduct(
        final Long productId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final List<Category> categories,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price
    ) {
        final Product product = em.find(Product.class, productId);
        if (product == null) {
            return false;
        }

        product.setTitle(title);
        product.setArtist(artist);
        product.setRecordLabel(normalizeRecordLabel(recordLabel));
        product.setCatalogNumber(normalizeRecordLabel(catalogNumber));
        product.setEditionCountry(normalizeRecordLabel(editionCountry));
        product.setCategories(categories);
        product.setDescription(description);
        product.setSleeveCondition(sleeveCondition);
        product.setRecordCondition(recordCondition);
        product.setPrice(price);

        return true;
    }

    @Override
    public boolean restoreUserDeletedProduct(final Long id) {
        return em.createQuery(
            "UPDATE Product SET state = :newState WHERE productId = :productId AND state = :currentState")
            .setParameter("newState", ProductState.ACTIVE.getPersistenceValue())
            .setParameter("productId", id)
            .setParameter("currentState", ProductState.USER_DELETED.getPersistenceValue())
            .executeUpdate() >= 1;
    }

    private static String normalizeRecordLabel(final String raw) {
        if (raw == null) {
            return "";
        }
        final String trimmed = raw.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private static String normalizeSuggestionQuery(final String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    private static String escapeForLike(final String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        final StringBuilder escaped = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            final char ch = raw.charAt(i);
            if (ch == '\\' || ch == '%' || ch == '_') {
                escaped.append('\\');
            }
            escaped.append(ch);
        }
        return escaped.toString();
    }
}
