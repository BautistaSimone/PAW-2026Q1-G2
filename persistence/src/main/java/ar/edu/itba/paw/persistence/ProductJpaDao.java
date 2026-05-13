package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
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

    private List<Category> findCategoriesByProductId(final Long productId) {
        return Collections.emptyList(); // TODO: Refactor methods that used this
    }

    private static String normalizeRecordLabel(final String recordLabel) {
        return recordLabel == null ? "" : recordLabel.trim();
    }

    /**
     * Escapes {@code \}, {@code %} and {@code _} for use inside a LIKE pattern with {@code ESCAPE '\\'}.
     */
    private static String escapeForLike(final String raw) {
        return raw.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    private static void appendConditionBucketSql(final StringBuilder sql, final ConditionBucket bucket) {
        final String avg = "(p.sleeve_condition + p.record_condition) / 2.0";
        switch (bucket) {
            case EXCELENTE -> sql.append(avg).append(" >= 9");
            case MUY_BUENO -> sql.append(avg).append(" >= 8 AND ").append(avg).append(" < 9");
            case BUENO -> sql.append(avg).append(" >= 7 AND ").append(avg).append(" < 8");
            case REGULAR -> sql.append(avg).append(" < 7");
        }
    }

    private static String orderBySql(final ProductSortOrder sortOrder) {
        return switch (sortOrder) {
            case NEWEST -> "p.published DESC, p.product_id DESC";
            case OLDEST -> "p.published ASC, p.product_id ASC";
            case PRICE_ASC -> "p.price ASC";
            case PRICE_DESC -> "p.price DESC";
            case NAME_ASC -> "LOWER(p.title) ASC";
            case NAME_DESC -> "LOWER(p.title) DESC";
            case CONDITION_DESC -> "(p.sleeve_condition + p.record_condition) / 2.0 DESC";
            case CONDITION_ASC -> "(p.sleeve_condition + p.record_condition) / 2.0 ASC";
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
        final List<Category> category,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final BigDecimal price
    ) {

        final LocalDate published = LocalDate.now();
        final String normalizedLabel = normalizeRecordLabel(recordLabel);
        final String normalizedCatNum = normalizeRecordLabel(catalogNumber);
        final String normalizedCountry = normalizeRecordLabel(editionCountry);

        final Product product = new Product(
            userId,
            title,
            artist,
            normalizedLabel,
            normalizedCatNum,
            normalizedCountry,
            category,
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
        // final StringBuilder whereSql = new StringBuilder("WHERE p.state = :state ");
        // final List<Object> args = new ArrayList<>();
        // args.add(ProductState.ACTIVE.getPersistenceValue());

        // if (criteria.getSearchText() != null && !criteria.getSearchText().isBlank()) {
        //     final String likeNeedle = escapeForLike(criteria.getSearchText().trim()).toLowerCase(Locale.ROOT);
        //     whereSql.append(" AND (");
        //     whereSql.append("LOWER(p.title) LIKE '%' || :title || '%' ESCAPE '\\' OR ");
        //     whereSql.append("LOWER(p.artist) LIKE '%' || :artist || '%' ESCAPE '\\' OR ");
        //     whereSql.append("LOWER(p.description) LIKE '%' || :description || '%' ESCAPE '\\'");
        //     whereSql.append(")");
        //     args.add(likeNeedle);
        //     args.add(likeNeedle);
        //     args.add(likeNeedle);
        // }

        // if (!criteria.getCategoryIds().isEmpty()) {
        //     whereSql.append(" AND EXISTS (SELECT 1 FROM products_categories pc WHERE pc.product_id = p.product_id AND pc.category_id IN (");
        //     whereSql.append(String.join(",", Collections.nCopies(criteria.getCategoryIds().size(), "?")));
        //     whereSql.append(")) ");
        //     args.addAll(criteria.getCategoryIds());
        // }

        // if (criteria.getMinPrice() != null) {
        //     whereSql.append(" AND p.price >= ? ");
        //     args.add(criteria.getMinPrice());
        // }

        // if (criteria.getMaxPrice() != null) {
        //     whereSql.append(" AND p.price <= ? ");
        //     args.add(criteria.getMaxPrice());
        // }

        // if (!criteria.getRecordLabels().isEmpty()) {
        //     whereSql.append(" AND p.record_label IN (");
        //     whereSql.append(String.join(",", Collections.nCopies(criteria.getRecordLabels().size(), "?")));
        //     whereSql.append(") ");
        //     args.addAll(criteria.getRecordLabels());
        // }

        // if (!criteria.getConditionBuckets().isEmpty()) {
        //     whereSql.append(" AND (");
        //     boolean first = true;
        //     for (ConditionBucket bucket : criteria.getConditionBuckets()) {
        //         if (!first) {
        //             whereSql.append(" OR ");
        //         }
        //         first = false;
        //         appendConditionBucketSql(whereSql, bucket);
        //     }
        //     whereSql.append(") ");
        // }

        // if (criteria.getUserId() != null) {
        //     whereSql.append(" AND p.user_id = ? ");
        //     args.add(criteria.getUserId());
        // }

        // final Long totalCount = jdbcTemplate.queryForObject(
        //     "SELECT COUNT(*) FROM products p " + whereSql.toString(),
        //     Long.class,
        //     args.toArray()
        // );

        // if (totalCount == null || totalCount == 0) {
        //     return new PaginatedResult<>(Collections.emptyList(), criteria.getPage(), criteria.getPageSize(), 0);
        // }

        // final StringBuilder selectSql = new StringBuilder(
        //     "SELECT p.product_id, p.user_id, p.title, p.artist, p.record_label, p.catalog_number, p.edition_country, p.description, " +
        //     "p.sleeve_condition, p.record_condition, p.published, p.price " +
        //     "FROM products p "
        // ).append(whereSql);

        // selectSql.append(" ORDER BY ").append(orderBySql(criteria.getSortOrder())).append(" LIMIT ? OFFSET ?");
        // args.add(criteria.getPageSize());
        // args.add((criteria.getPage() - 1) * criteria.getPageSize());

        // final List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql.toString(), args.toArray());
        // final List<Product> products = rows.stream().map(this::mapProductFromRow).collect(Collectors.toList());

        // return new PaginatedResult<>(products, criteria.getPage(), criteria.getPageSize(), totalCount);
        return new PaginatedResult<>(Collections.emptyList(), 1, 0, 0);
    }

    @Override
    public PaginatedResult<Product> findProductsByUserIdAndState(
        final Long userId,
        final ProductState state,
        final int page,
        final int pageSize
    ) {
        // final int safePage = page < 1 ? 1 : page;
        // final int safePageSize = pageSize < 1 ? 12 : pageSize;
        // final String stateVal = state.getPersistenceValue();

        // final String whereSql = "WHERE p.user_id = ? AND p.state = ? ";
        // final Long totalCount = jdbcTemplate.queryForObject(
        //     "SELECT COUNT(*) FROM products p " + whereSql,
        //     Long.class,
        //     userId,
        //     stateVal
        // );

        // if (totalCount == null || totalCount == 0) {
        //     return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        // }

        // final List<Object> selectArgs = new ArrayList<>();
        // selectArgs.add(userId);
        // selectArgs.add(stateVal);
        // final String selectSql =
        //     "SELECT p.product_id, p.user_id, p.title, p.artist, p.record_label, p.catalog_number, p.edition_country, p.description, " +
        //     "p.sleeve_condition, p.record_condition, p.published, p.price " +
        //     "FROM products p " + whereSql +
        //     "ORDER BY " + orderBySql(ProductSortOrder.NEWEST) + " LIMIT ? OFFSET ?";
        // selectArgs.add(safePageSize);
        // selectArgs.add((safePage - 1) * safePageSize);

        // final List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, selectArgs.toArray());
        // final List<Product> products = rows.stream().map(this::mapProductFromRow).collect(Collectors.toList());

        // return new PaginatedResult<>(products, safePage, safePageSize, totalCount);
        return new PaginatedResult<>(Collections.emptyList(), 1, 0, 0);
    }

    @Override
    public List<String> listDistinctArtists() {
        final TypedQuery<String> query = em.createQuery("SELECT DISTINCT TRIM(artist) AS value FROM Product WHERE" +
        " artist IS NOT NULL AND state = :state AND TRIM(artist) <> '' ORDER BY value ASC", String.class);
        query.setParameter("state", ProductState.ACTIVE.getPersistenceValue());
        return query.getResultList();
    }

    @Override
    public List<String> listDistinctRecordLabels() {
        final TypedQuery<String> query = em.createQuery("SELECT DISTINCT TRIM(record_label) AS value FROM Product WHERE" +
        " record_label IS NOT NULL AND state = :state AND TRIM(record_label) <> '' ORDER BY value ASC", String.class);
        query.setParameter("state", ProductState.ACTIVE.getPersistenceValue());
        return query.getResultList();
    }

    @Override
    public Optional<Product> findByIdIfAvailable(final Long id) {
        final TypedQuery<Product> query = em.createQuery("FROM Product WHERE productId = :product_id AND state = :state", Product.class);
        query.setParameter("product_id", id);
        query.setParameter("state", ProductState.ACTIVE.getPersistenceValue());
        return query.getResultList().stream().findFirst();
    }

    @Override
    public boolean reserveIfAvailable(final Long id) {
        return em.createQuery(
            "UPDATE Product SET state = :state WHERE productId = :product_id AND state = :state_cond")
            .setParameter("state", ProductState.RESERVED.getPersistenceValue())
            .setParameter("product_id", id)
            .setParameter("state_cond", ProductState.ACTIVE.getPersistenceValue())
            .executeUpdate() >= 1;
    }

    @Override
    public void markAsSold(final Long id) {
        em.createQuery(
            "UPDATE Product SET state = :state WHERE productId = :product_id AND state = :state_cond")
            .setParameter("state", ProductState.SOLD.getPersistenceValue())
            .setParameter("product_id", id)
            .setParameter("state_cond", ProductState.RESERVED.getPersistenceValue())
            .executeUpdate();
    }

    @Override
    public boolean markAsUserDeleted(final Long id) {
        return em.createQuery(
            "UPDATE Product SET state = :state WHERE productId = :product_id AND state = :state_cond")
            .setParameter("state", ProductState.USER_DELETED.getPersistenceValue())
            .setParameter("product_id", id)
            .setParameter("state_cond", ProductState.ACTIVE.getPersistenceValue())
            .executeUpdate() >= 1;
    }

    @Override
    public void markAsAdminHidden(final Long id) {
        em.createQuery(
            "UPDATE Product SET state = :state WHERE productId = :product_id")
            .setParameter("state", ProductState.ADMIN_HIDDEN.getPersistenceValue())
            .setParameter("product_id", id)
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
        final String normalizedLabel = normalizeRecordLabel(recordLabel);
        final String normalizedCatNum = normalizeRecordLabel(catalogNumber);
        final String normalizedCountry = normalizeRecordLabel(editionCountry);

        Optional<Product> productOpt = findById(productId);

        if (!productOpt.isPresent())
            return false;

        final Product product = productOpt.get();

        product.setTitle(title);
        product.setArtist(artist);
        product.setRecordLabel(recordLabel);
        product.setCatalogNumber(catalogNumber);
        product.setEditionCountry(editionCountry);
        product.setCategories(categories);
        product.setDescription(description);
        product.setSleeveCondition(sleeveCondition);
        product.setRecordCondition(recordCondition);
        product.setPrice(price);

        em.refresh(product);

        return true;
    }

    @Override
    public boolean restoreUserDeletedProduct(final Long id) {

        return em.createQuery(
            "UPDATE Product SET state = :state WHERE productId = :product_id AND state = :state_cond")
            .setParameter("state", ProductState.ACTIVE.getPersistenceValue())
            .setParameter("product_id", id)
            .setParameter("state_cond", ProductState.USER_DELETED.getPersistenceValue())
            .executeUpdate() >= 1;
    }
}
