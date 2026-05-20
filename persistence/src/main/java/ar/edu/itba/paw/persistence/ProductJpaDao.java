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
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.ProductState;

@Repository
public class ProductJpaDao implements ProductDao {

    @PersistenceContext
    private EntityManager em;

    private static final int MIN_SUGGESTION_QUERY_LENGTH = 2;

    private static String normalizeRecordLabel(final String recordLabel) {
        return recordLabel == null ? "" : recordLabel.trim();
    }

    private static String escapeForLike(final String raw) {
        return raw.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    private static String normalizeSuggestionQuery(final String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase();
    }

    private static void appendConditionBucketJpql(final StringBuilder jpql, final ConditionBucket bucket) {
        final String avg = "(p.sleeveCondition + p.recordCondition) / 2.0";
        switch (bucket) {
            case EXCELENTE -> jpql.append(avg).append(" >= 9");
            case MUY_BUENO -> jpql.append(avg).append(" >= 8 AND ").append(avg).append(" < 9");
            case BUENO -> jpql.append(avg).append(" >= 7 AND ").append(avg).append(" < 8");
            case REGULAR -> jpql.append(avg).append(" < 7");
        }
    }

    private static String orderByJpql(final ProductSortOrder sortOrder) {
        return switch (sortOrder) {
            case NEWEST -> "p.published DESC, p.productId DESC";
            case OLDEST -> "p.published ASC, p.productId ASC";
            case PRICE_ASC -> "p.price ASC";
            case PRICE_DESC -> "p.price DESC";
            case NAME_ASC -> "LOWER(p.title) ASC";
            case NAME_DESC -> "LOWER(p.title) DESC";
            case CONDITION_DESC -> "(p.sleeveCondition + p.recordCondition) / 2.0 DESC";
            case CONDITION_ASC -> "(p.sleeveCondition + p.recordCondition) / 2.0 ASC";
        };
    }

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
            conditions.add(cb.greaterThan(root.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            conditions.add(cb.lessThan(root.get("price"), criteria.getMinPrice()));
        }

        // if (criteria.getSearchText() != null && !criteria.getSearchText().isBlank()) {
        //     final String likeNeedle = "%" + escapeForLike(criteria.getSearchText().trim()).toLowerCase() + "%";
        //     whereJpql.append(" AND (");
        //     whereJpql.append("LOWER(p.title) LIKE :searchText ESCAPE '\\' OR ");
        //     whereJpql.append("LOWER(p.artist) LIKE :searchText ESCAPE '\\' OR ");
        //     whereJpql.append("LOWER(p.description) LIKE :searchText ESCAPE '\\'");
        //     whereJpql.append(")");
        //     paramNames.add("searchText");
        //     paramValues.add(likeNeedle);
        // }

        // if (!criteria.getCategoryIds().isEmpty()) {
        //     whereJpql.append(" AND EXISTS (SELECT 1 FROM Product p2 JOIN p2.categories c WHERE p2 = p AND c.id IN :categoryIds)");
        //     paramNames.add("categoryIds");
        //     paramValues.add(criteria.getCategoryIds());
        // }

        // if (!criteria.getRecordLabels().isEmpty()) {
        //     whereJpql.append(" AND p.recordLabel IN :recordLabels");
        //     paramNames.add("recordLabels");
        //     paramValues.add(criteria.getRecordLabels());
        // }

        // if (!criteria.getConditionBuckets().isEmpty()) {
        //     whereJpql.append(" AND (");
        //     boolean first = true;
        //     for (ConditionBucket bucket : criteria.getConditionBuckets()) {
        //         if (!first) {
        //             whereJpql.append(" OR ");
        //         }
        //         first = false;
        //         appendConditionBucketJpql(whereJpql, bucket);
        //     }
        //     whereJpql.append(")");
        // }

        // if (criteria.getUserId() != null) {
        //     whereJpql.append(" AND p.userId = :userId");
        //     paramNames.add("userId");
        //     paramValues.add(criteria.getUserId());
        // }

        // if (!criteria.getUserIds().isEmpty()) {
        //     whereJpql.append(" AND p.userId IN :userIds");
        //     paramNames.add("userIds");
        //     paramValues.add(criteria.getUserIds());
        // }

        // final TypedQuery<Long> countQuery = em.createQuery(
        //     "SELECT COUNT(p) FROM Product p " + whereJpql, Long.class
        // );
        // for (int i = 0; i < paramNames.size(); i++) {
        //     countQuery.setParameter(paramNames.get(i), paramValues.get(i));
        // }
        // final long totalCount = countQuery.getSingleResult();

        // if (totalCount == 0) {
        //     return new PaginatedResult<>(Collections.emptyList(), criteria.getPage(), criteria.getPageSize(), 0);
        // }

        // final TypedQuery<Product> selectQuery = em.createQuery(
        //     "SELECT p FROM Product p " + whereJpql + " ORDER BY " + orderByJpql(criteria.getSortOrder()),
        //     Product.class
        // );
        // for (int i = 0; i < paramNames.size(); i++) {
        //     selectQuery.setParameter(paramNames.get(i), paramValues.get(i));
        // }
        // selectQuery.setMaxResults(criteria.getPageSize());
        // selectQuery.setFirstResult((criteria.getPage() - 1) * criteria.getPageSize());

        // Fetches the count of all Products as per given criteria
        query.where(cb.and(conditions.toArray(new Predicate[0])));

        // This query fetches the Products as per the Page Limit
        List<Product> result = em.createQuery(query)
            .setFirstResult((criteria.getPage() - 1) * criteria.getPageSize())
            .setMaxResults(criteria.getPageSize())
            .getResultList();
        
        // Create Count Query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> productRootCount = countQuery.from(Product.class);
        countQuery.select(cb.count(productRootCount)).where(cb.and(conditions.toArray(new Predicate[conditions.size()])));

        Long count = em.createQuery(countQuery).getSingleResult();

        return new PaginatedResult<>(result, criteria.getPage(), criteria.getPageSize(), count);
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
}
