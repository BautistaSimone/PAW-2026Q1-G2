package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

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
public class ProductJdbcDao implements ProductDao {

    private static final RowMapper<Category> CATEGORY_ROW_MAPPER = (rs, rowNum) ->
        new Category(
            rs.getLong("category_id"),
            rs.getString("name")
        );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert productInsert;
    private final SimpleJdbcInsert productCategoryInsert;

    @Autowired
    public ProductJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.productInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("products")
            .usingGeneratedKeyColumns("product_id");
        this.productCategoryInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("products_categories");
    }

    private List<Category> findCategoriesByProductId(final Long productId) {
        return jdbcTemplate.query(
            "SELECT c.category_id, c.name FROM categories c " +
            "JOIN products_categories pc ON c.category_id = pc.category_id " +
            "WHERE pc.product_id = ? ORDER BY c.name ASC",
            CATEGORY_ROW_MAPPER, productId
        );
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

    private Product mapProduct(
        final Long productId,
        final Long userId,
        final String title,
        final String artist,
        final String recordLabel,
        final String catalogNumber,
        final String editionCountry,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final LocalDate published,
        final BigDecimal price
    ) {
        final List<Category> categories = findCategoriesByProductId(productId);
        return new Product(
            productId, userId, title, artist, recordLabel, catalogNumber, editionCountry, categories, description,
            sleeveCondition, recordCondition, published, price
        );
    }

    private Product mapProductFromRow(final Map<String, Object> row) {
        final String label = Optional.ofNullable((String) row.get("record_label")).orElse("");
        final String catNum = Optional.ofNullable((String) row.get("catalog_number")).orElse("");
        final String country = Optional.ofNullable((String) row.get("edition_country")).orElse("");
        return mapProduct(
            ((Number) row.get("product_id")).longValue(),
            ((Number) row.get("user_id")).longValue(),
            (String) row.get("title"),
            (String) row.get("artist"),
            label,
            catNum,
            country,
            (String) row.get("description"),
            (BigDecimal) row.get("sleeve_condition"),
            (BigDecimal) row.get("record_condition"),
            ((Date) row.get("published")).toLocalDate(),
            (BigDecimal) row.get("price")
        );
    }

    private void insertProductCategories(final Long productId, final List<Long> categoryIds) {
        if (categoryIds == null) {
            return;
        }
        for (Long categoryId : categoryIds) {
            final Map<String, Object> pcValues = new HashMap<>();
            pcValues.put("product_id", productId);
            pcValues.put("category_id", categoryId);
            productCategoryInsert.execute(pcValues);
        }
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
        final Map<String, Object> values = new HashMap<>();
        final LocalDate published = LocalDate.now();
        final String normalizedLabel = normalizeRecordLabel(recordLabel);
        final String normalizedCatNum = normalizeRecordLabel(catalogNumber);
        final String normalizedCountry = normalizeRecordLabel(editionCountry);

        values.put("user_id", userId);
        values.put("title", title);
        values.put("artist", artist);
        values.put("record_label", normalizedLabel);
        values.put("catalog_number", normalizedCatNum);
        values.put("edition_country", normalizedCountry);
        values.put("sleeve_condition", sleeveCondition);
        values.put("record_condition", recordCondition);
        values.put("price", price);
        values.put("description", description);
        values.put("published", Date.valueOf(published));
        values.put("state", ProductState.ACTIVE.getPersistenceValue());

        final Number id = productInsert.executeAndReturnKey(values);
        final Long productId = id.longValue();

        insertProductCategories(productId, categoryIds);

        return mapProduct(
            productId, userId, title, artist, normalizedLabel, normalizedCatNum, normalizedCountry, description,
            sleeveCondition, recordCondition, published, price
        );
    }

    @Override
    public PaginatedResult<Product> listProducts() {
        return findProducts(ProductSearchCriteria.empty());
    }

    @Override
    public PaginatedResult<Product> findProducts(final ProductSearchCriteria criteria) {
        final StringBuilder whereSql = new StringBuilder("WHERE p.state = ? ");
        final List<Object> args = new ArrayList<>();
        args.add(ProductState.ACTIVE.getPersistenceValue());

        if (criteria.getSearchText() != null && !criteria.getSearchText().isBlank()) {
            final String likeNeedle = escapeForLike(criteria.getSearchText().trim()).toLowerCase(Locale.ROOT);
            whereSql.append(" AND (");
            whereSql.append("LOWER(p.title) LIKE '%' || ? || '%' ESCAPE '\\' OR ");
            whereSql.append("LOWER(p.artist) LIKE '%' || ? || '%' ESCAPE '\\' OR ");
            whereSql.append("LOWER(p.description) LIKE '%' || ? || '%' ESCAPE '\\'");
            whereSql.append(")");
            args.add(likeNeedle);
            args.add(likeNeedle);
            args.add(likeNeedle);
        }

        if (!criteria.getCategoryIds().isEmpty()) {
            whereSql.append(" AND EXISTS (SELECT 1 FROM products_categories pc WHERE pc.product_id = p.product_id AND pc.category_id IN (");
            whereSql.append(String.join(",", Collections.nCopies(criteria.getCategoryIds().size(), "?")));
            whereSql.append(")) ");
            args.addAll(criteria.getCategoryIds());
        }

        if (criteria.getMinPrice() != null) {
            whereSql.append(" AND p.price >= ? ");
            args.add(criteria.getMinPrice());
        }

        if (criteria.getMaxPrice() != null) {
            whereSql.append(" AND p.price <= ? ");
            args.add(criteria.getMaxPrice());
        }

        if (!criteria.getRecordLabels().isEmpty()) {
            whereSql.append(" AND p.record_label IN (");
            whereSql.append(String.join(",", Collections.nCopies(criteria.getRecordLabels().size(), "?")));
            whereSql.append(") ");
            args.addAll(criteria.getRecordLabels());
        }

        if (!criteria.getConditionBuckets().isEmpty()) {
            whereSql.append(" AND (");
            boolean first = true;
            for (ConditionBucket bucket : criteria.getConditionBuckets()) {
                if (!first) {
                    whereSql.append(" OR ");
                }
                first = false;
                appendConditionBucketSql(whereSql, bucket);
            }
            whereSql.append(") ");
        }

        if (criteria.getUserId() != null) {
            whereSql.append(" AND p.user_id = ? ");
            args.add(criteria.getUserId());
        }

        final Long totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM products p " + whereSql.toString(),
            Long.class,
            args.toArray()
        );

        if (totalCount == null || totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), criteria.getPage(), criteria.getPageSize(), 0);
        }

        final StringBuilder selectSql = new StringBuilder(
            "SELECT p.product_id, p.user_id, p.title, p.artist, p.record_label, p.catalog_number, p.edition_country, p.description, " +
            "p.sleeve_condition, p.record_condition, p.published, p.price " +
            "FROM products p "
        ).append(whereSql);

        selectSql.append(" ORDER BY ").append(orderBySql(criteria.getSortOrder())).append(" LIMIT ? OFFSET ?");
        args.add(criteria.getPageSize());
        args.add((criteria.getPage() - 1) * criteria.getPageSize());

        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql.toString(), args.toArray());
        final List<Product> products = rows.stream().map(this::mapProductFromRow).collect(Collectors.toList());

        return new PaginatedResult<>(products, criteria.getPage(), criteria.getPageSize(), totalCount);
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

        final String whereSql = "WHERE p.user_id = ? AND p.state = ? ";
        final Long totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM products p " + whereSql,
            Long.class,
            userId,
            stateVal
        );

        if (totalCount == null || totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safePageSize, 0);
        }

        final List<Object> selectArgs = new ArrayList<>();
        selectArgs.add(userId);
        selectArgs.add(stateVal);
        final String selectSql =
            "SELECT p.product_id, p.user_id, p.title, p.artist, p.record_label, p.catalog_number, p.edition_country, p.description, " +
            "p.sleeve_condition, p.record_condition, p.published, p.price " +
            "FROM products p " + whereSql +
            "ORDER BY " + orderBySql(ProductSortOrder.NEWEST) + " LIMIT ? OFFSET ?";
        selectArgs.add(safePageSize);
        selectArgs.add((safePage - 1) * safePageSize);

        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, selectArgs.toArray());
        final List<Product> products = rows.stream().map(this::mapProductFromRow).collect(Collectors.toList());

        return new PaginatedResult<>(products, safePage, safePageSize, totalCount);
    }

    @Override
    public List<String> listDistinctArtists() {
        return jdbcTemplate.query(
            "SELECT DISTINCT TRIM(artist) AS value FROM products WHERE state = ? " +
            "AND artist IS NOT NULL AND TRIM(artist) <> '' ORDER BY value ASC",
            (rs, rowNum) -> rs.getString(1),
            ProductState.ACTIVE.getPersistenceValue()
        );
    }

    @Override
    public List<String> listDistinctRecordLabels() {
        return jdbcTemplate.query(
            "SELECT DISTINCT TRIM(record_label) AS value FROM products WHERE state = ? " +
            "AND record_label IS NOT NULL AND TRIM(record_label) <> '' ORDER BY value ASC",
            (rs, rowNum) -> rs.getString(1),
            ProductState.ACTIVE.getPersistenceValue()
        );
    }

    @Override
    public Optional<Product> findById(final Long id) {
        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT product_id, user_id, title, artist, record_label, catalog_number, edition_country, description, sleeve_condition, record_condition, " +
            "published, price FROM products WHERE product_id = ?",
            id
        );

        return rows.stream().findFirst().map(this::mapProductFromRow);
    }

    @Override
    public Optional<Product> findByIdIfAvailable(final Long id) {
        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT product_id, user_id, title, artist, record_label, catalog_number, edition_country, description, sleeve_condition, record_condition, " +
            "published, price FROM products WHERE product_id = ? AND state = ?",
            id,
            ProductState.ACTIVE.getPersistenceValue()
        );

        return rows.stream().findFirst().map(this::mapProductFromRow);
    }

    @Override
    public boolean reserveIfAvailable(final Long id) {
        return jdbcTemplate.update(
            "UPDATE products SET state = ? WHERE product_id = ? AND state = ?",
            ProductState.RESERVED.getPersistenceValue(),
            id,
            ProductState.ACTIVE.getPersistenceValue()
        ) == 1;
    }

    @Override
    public void markAsSold(final Long id) {
        jdbcTemplate.update(
            "UPDATE products SET state = ? WHERE product_id = ? AND state = ?",
            ProductState.SOLD.getPersistenceValue(),
            id,
            ProductState.RESERVED.getPersistenceValue()
        );
    }

    @Override
    public boolean markAsUserDeleted(final Long id) {
        return jdbcTemplate.update(
            "UPDATE products SET state = ? WHERE product_id = ? AND state = ?",
            ProductState.USER_DELETED.getPersistenceValue(),
            id,
            ProductState.ACTIVE.getPersistenceValue()
        ) == 1;
    }

    @Override
    public void markAsAdminHidden(final Long id) {
        jdbcTemplate.update(
            "UPDATE products SET state = ? WHERE product_id = ?",
            ProductState.ADMIN_HIDDEN.getPersistenceValue(),
            id
        );
    }

    @Override
    public boolean updateProduct(
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
        final String normalizedLabel = normalizeRecordLabel(recordLabel);
        final String normalizedCatNum = normalizeRecordLabel(catalogNumber);
        final String normalizedCountry = normalizeRecordLabel(editionCountry);

        final int updated = jdbcTemplate.update(
            "UPDATE products SET title = ?, artist = ?, record_label = ?, catalog_number = ?, edition_country = ?, " +
            "description = ?, sleeve_condition = ?, record_condition = ?, price = ? " +
            "WHERE product_id = ? AND state = ?",
            title,
            artist,
            normalizedLabel,
            normalizedCatNum,
            normalizedCountry,
            description,
            sleeveCondition,
            recordCondition,
            price,
            productId,
            ProductState.ACTIVE.getPersistenceValue()
        );

        if (updated != 1) {
            return false;
        }

        jdbcTemplate.update("DELETE FROM products_categories WHERE product_id = ?", productId);
        insertProductCategories(productId, categoryIds);
        return true;
    }

    @Override
    public boolean restoreUserDeletedProduct(final Long id) {
        return jdbcTemplate.update(
            "UPDATE products SET state = ? WHERE product_id = ? AND state = ?",
            ProductState.ACTIVE.getPersistenceValue(),
            id,
            ProductState.USER_DELETED.getPersistenceValue()
        ) == 1;
    }
}
