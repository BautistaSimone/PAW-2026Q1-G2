package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.JoinType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.ConditionBucket;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSortOrder;
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
    @SuppressWarnings("unchecked")
    public List<Product> findByIds(java.util.Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return em.createQuery(
            "FROM Product p LEFT JOIN FETCH p.categories WHERE p.productId IN :ids", Product.class
        )
        .setParameter("ids", ids)
        .getResultList();
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
        final BigDecimal price,
        final int stock
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
            price,
            stock
        );

        em.persist(product);
        return product;
    }

    @Override
    public PaginatedResult<Product> listProducts() {
        return findProducts(ProductSearchCriteria.empty());
    }
        
    private String orderBySql(ProductSortOrder sortOrder) {
        return switch (sortOrder) {

            case NEWEST ->
                "p.published DESC";

            case OLDEST ->
                "p.published ASC";

            case PRICE_ASC ->
                "p.price ASC";

            case PRICE_DESC ->
                "p.price DESC";

            case NAME_ASC ->
                "LOWER(p.title) ASC";

            case NAME_DESC ->
                "LOWER(p.title) DESC";

            case CONDITION_ASC ->
                "(p.sleeve_condition + p.record_condition)/2 ASC";

            case CONDITION_DESC ->
                "(p.sleeve_condition + p.record_condition)/2 DESC";
        } + ", p.product_id ASC";
    }

    @Override
    public PaginatedResult<Product> findProducts(final ProductSearchCriteria criteria) {
        final StringBuilder whereSql = new StringBuilder("WHERE p.state = :state");
        final Map<String, Object> params = new HashMap<>();
        params.put("state", ProductState.ACTIVE.getPersistenceValue());

        // Search text filter
        if (criteria.getSearchText() != null && !criteria.getSearchText().isBlank()) {
            String raw = criteria.getSearchText().trim().toLowerCase();
            boolean needsEscape = raw.contains("%") || raw.contains("_") || raw.contains("\\");
            String needle = "%" + (needsEscape ? escapeForLike(raw) : raw) + "%";
            whereSql.append(" AND (LOWER(p.title) LIKE :searchText ESCAPE '\\' ")
                    .append("OR LOWER(p.artist) LIKE :searchText ESCAPE '\\' ")
                    .append("OR LOWER(p.description) LIKE :searchText ESCAPE '\\')");
            params.put("searchText", needle);
        }

        // Price filter
        if (criteria.getMinPrice() != null) {
            whereSql.append(" AND p.price >= :minPrice");
            params.put("minPrice", criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            whereSql.append(" AND p.price <= :maxPrice");
            params.put("maxPrice", criteria.getMaxPrice());
        }

        // Category filter
        if (!criteria.getCategoryIds().isEmpty()) {
            whereSql.append(" AND EXISTS (SELECT 1 FROM products_categories pc WHERE pc.product_id = p.product_id AND pc.category_id IN :categoryIds)");
            params.put("categoryIds", criteria.getCategoryIds());
        }

        // Record label filter
        if (!criteria.getRecordLabels().isEmpty()) {
            whereSql.append(" AND p.record_label IN :recordLabels");
            params.put("recordLabels", criteria.getRecordLabels());
        }

        // ConditionBuckets filter (average of sleeveCondition + recordCondition)/2
        if (!criteria.getConditionBuckets().isEmpty()) {
            whereSql.append(" AND (");
            boolean first = true;
            for (ConditionBucket bucket : criteria.getConditionBuckets()) {
                if (!first) whereSql.append(" OR ");
                first = false;
                switch (bucket) {
                    case EXCELENTE -> whereSql.append("( (p.sleeve_condition + p.record_condition)/2 >= 9.0 )");
                    case MUY_BUENO -> whereSql.append("( (p.sleeve_condition + p.record_condition)/2 >= 8.0 AND (p.sleeve_condition + p.record_condition)/2 < 9.0 )");
                    case BUENO -> whereSql.append("( (p.sleeve_condition + p.record_condition)/2 >= 7.0 AND (p.sleeve_condition + p.record_condition)/2 < 8.0 )");
                    case REGULAR -> whereSql.append("( (p.sleeve_condition + p.record_condition)/2 < 7.0 )");
                }
            }
            whereSql.append(")");
        }

        // User filters
        if (criteria.getUserId() != null) {
            whereSql.append(" AND p.user_id = :userId");
            params.put("userId", criteria.getUserId());
        }
        if (!criteria.getUserIds().isEmpty()) {
            whereSql.append(" AND p.user_id IN :userIds");
            params.put("userIds", criteria.getUserIds());
        }

        // Get count
        final String countSql = "SELECT COUNT(*) FROM products p " + whereSql;
        final Query countQuery = em.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);
        final long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), criteria.getPage(), criteria.getPageSize(), 0);
        }

        // Fetch IDs only (1+1 pattern)
        final int safePage = criteria.getPage() < 1 ? 1 : criteria.getPage();
        final int safePageSize = criteria.getPageSize() < 1 ? 12 : criteria.getPageSize();

        final String idsSql = "SELECT p.product_id FROM products p "
                + whereSql
                + " ORDER BY " + orderBySql(criteria.getSortOrder());

        final Query idsQuery = em.createNativeQuery(idsSql);
        params.forEach(idsQuery::setParameter);

        @SuppressWarnings("unchecked")
        List<Number> ids = idsQuery
            .setFirstResult((safePage-1) * safePageSize)
            .setMaxResults(safePageSize)
            .getResultList();

        boolean hasNext = ids.size() > criteria.getPageSize();
        if (hasNext) {
            ids = ids.subList(0, criteria.getPageSize());
        }

        // Fetch full entities by IDs
        if (ids.isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), criteria.getPage(), criteria.getPageSize(), totalCount);
        }

        final TypedQuery<Product> selectQuery = em.createQuery("FROM Product p LEFT JOIN FETCH p.categories WHERE p.productId IN :ids", Product.class)
            .setParameter("ids", ids.stream().map(Number::longValue).collect(Collectors.toList()));

        // Mantain ordering
        final Map<Long, Product> productsById = selectQuery.getResultList().stream()
            .collect(Collectors.toMap(Product::getId, product -> product, (existing, replacement) -> existing));
            
        final List<Product> orderedProducts = ids.stream()
            .map(Number::longValue)
            .map(productsById::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return new PaginatedResult<>(orderedProducts, safePage, safePageSize, totalCount);
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

        final Number countResult = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM products WHERE user_id = :userId AND state = :state")
            .setParameter("userId", userId)
            .setParameter("state", stateVal)
            .getSingleResult();
        final long totalCount = countResult == null ? 0L : countResult.longValue();
        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        // Paginate with 1 + 1 queries
        @SuppressWarnings("unchecked")
        List<Number> ids = em.createNativeQuery(
                "SELECT product_id FROM products WHERE user_id = :userId AND state = :state ORDER BY published DESC, product_id DESC")
            .setParameter("userId", userId)
            .setParameter("state", stateVal)
            .setFirstResult((safePage-1) * safePageSize)
            .setMaxResults(safePageSize)
            .getResultList();

        if (ids.isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, totalCount);
        }

        final TypedQuery<Product> selectQuery = em.createQuery("FROM Product p LEFT JOIN FETCH p.categories WHERE p.productId IN :ids", Product.class)
            .setParameter("ids", ids.stream().map(Number::longValue).collect(Collectors.toList()));

        final Map<Long, Product> productsById = selectQuery.getResultList().stream()
            .collect(Collectors.toMap(Product::getId, product -> product, (existing, replacement) -> existing));
        final List<Product> orderedProducts = ids.stream()
            .map(Number::longValue)
            .map(productsById::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return new PaginatedResult<>(orderedProducts, safePage, safePageSize, totalCount);
    }

    @Override
    public PaginatedResult<Product> findActiveProductsByUserId(
        final Long userId,
        final int page,
        final int pageSize
    ) {
        final int safePage = page < 1 ? 1 : page;
        final int safePageSize = pageSize < 1 ? 12 : pageSize;

        if (userId == null) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final String activeState = ProductState.ACTIVE.getPersistenceValue();
        final long totalCount = em.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.userId = :userId AND p.state = :state",
                Long.class)
            .setParameter("userId", userId)
            .setParameter("state", activeState)
            .getSingleResult();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final List<Product> products = em.createQuery(
                "FROM Product p " +
                "WHERE p.userId = :userId AND p.state = :state " +
                "ORDER BY p.published DESC, p.productId DESC",
                Product.class)
            .setParameter("userId", userId)
            .setParameter("state", activeState)
            .setFirstResult((safePage - 1) * safePageSize)
            .setMaxResults(safePageSize)
            .getResultList();

        return new PaginatedResult<>(products, safePage, safePageSize, totalCount);
    }

    @Override
    public Map<Long, Long> countActiveProductsByUserIds(final List<Long> userIds) {
        final Map<Long, Long> counts = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return counts;
        }
        for (Long userId : userIds) {
            if (userId != null) {
                counts.put(userId, 0L);
            }
        }
        if (counts.isEmpty()) {
            return counts;
        }

        final List<Object[]> rows = em.createQuery(
                "SELECT p.userId, COUNT(p) " +
                "FROM Product p " +
                "WHERE p.userId IN :ids AND p.state = :state " +
                "GROUP BY p.userId",
                Object[].class)
            .setParameter("ids", counts.keySet())
            .setParameter("state", ProductState.ACTIVE.getPersistenceValue())
            .getResultList();

        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    public Map<Long, List<Product>> findLatestActiveProductsByUserIds(
        final List<Long> userIds,
        final int perUserLimit
    ) {
        final Map<Long, List<Product>> productsByUserId = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty() || perUserLimit < 1) {
            return productsByUserId;
        }

        final List<Long> distinctUserIds = userIds.stream()
            .filter(productId -> productId != null)
            .distinct()
            .collect(Collectors.toList());

        for (Long userId : distinctUserIds) {
            productsByUserId.put(userId, new ArrayList<>());
        }
        if (productsByUserId.isEmpty()) {
            return productsByUserId;
        }

        final List<Product> products = em.createQuery(
                "FROM Product p " +
                "WHERE p.state = :state AND p.userId IN :ids " +
                "AND (" +
                " SELECT COUNT(newer) FROM Product newer " +
                " WHERE newer.state = :state " +
                " AND newer.userId = p.userId " +
                " AND (" +
                "  newer.published > p.published " +
                "  OR (newer.published = p.published AND newer.productId >= p.productId)" +
                " )" +
                ") <= :limit " +
                "ORDER BY p.userId ASC, p.published DESC, p.productId DESC",
                Product.class)
            .setParameter("state", ProductState.ACTIVE.getPersistenceValue())
            .setParameter("ids", distinctUserIds)
            .setParameter("limit", (long) perUserLimit)
            .getResultList();

        for (Product product : products) {
            productsByUserId.computeIfAbsent(product.getUserId(), productId -> new ArrayList<>()).add(product);
        }
        return productsByUserId;
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
            "FROM Product p LEFT JOIN FETCH p.categories WHERE p.productId = :productId AND p.state = :state", Product.class
        );
        query.setParameter("productId", id);
        query.setParameter("state", ProductState.ACTIVE.getPersistenceValue());
        return query.getResultList().stream().findFirst();
    }

    @Override
    public boolean decrementStock(final Long id) {
        return em.createQuery(
            "UPDATE Product SET stock = stock - 1, " +
            "state = CASE WHEN stock - 1 = 0 THEN :soldState ELSE state END " +
            "WHERE productId = :productId AND state = :activeState AND stock > 0")
            .setParameter("soldState", ProductState.SOLD.getPersistenceValue())
            .setParameter("productId", id)
            .setParameter("activeState", ProductState.ACTIVE.getPersistenceValue())
            .executeUpdate() >= 1;
    }

    @Override
    public boolean incrementStock(final Long id) {
        return em.createQuery(
            "UPDATE Product SET stock = stock + 1, state = :activeState " +
            "WHERE productId = :productId AND state IN (:activeState, :soldState)")
            .setParameter("activeState", ProductState.ACTIVE.getPersistenceValue())
            .setParameter("soldState", ProductState.SOLD.getPersistenceValue())
            .setParameter("productId", id)
            .executeUpdate() >= 1;
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
        final BigDecimal price,
        final int stock
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
        product.setStock(stock);

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

    @Override
    public int markAllAsAdminHiddenByUserId(final Long userId) {
        return em.createQuery(
            "UPDATE Product p SET p.state = :newState " +
            "WHERE p.userId = :userId AND p.state = :activeState")
            .setParameter("newState", ProductState.ADMIN_HIDDEN.getPersistenceValue())
            .setParameter("userId", userId)
            .setParameter("activeState", ProductState.ACTIVE.getPersistenceValue())
            .executeUpdate();
    }
}
