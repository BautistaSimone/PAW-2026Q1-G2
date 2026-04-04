package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;

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

    private static void appendConditionBucketSql(final StringBuilder sql, final ConditionBucket bucket) {
        final String avg = "(p.sleeve_condition + p.record_condition) / 2.0";
        switch (bucket) {
            case EXCELENTE -> sql.append(avg).append(" >= 9");
            case MUY_BUENO -> sql.append(avg).append(" >= 8 AND ").append(avg).append(" < 9");
            case BUENO -> sql.append(avg).append(" >= 7 AND ").append(avg).append(" < 8");
            case REGULAR -> sql.append(avg).append(" < 7");
        }
    }

    private Product mapProduct(
        final Long productId,
        final Long userId,
        final String title,
        final String artist,
        final String recordLabel,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final String neighborhood,
        final String province,
        final LocalDate published,
        final BigDecimal price
    ) {
        final List<Category> categories = findCategoriesByProductId(productId);
        return new Product(
            productId, userId, title, artist, recordLabel, categories, description,
            sleeveCondition, recordCondition, neighborhood, province, published, price
        );
    }

    private Product mapProductFromRow(final Map<String, Object> row) {
        final String label = Optional.ofNullable((String) row.get("record_label")).orElse("");
        return mapProduct(
            ((Number) row.get("product_id")).longValue(),
            ((Number) row.get("user_id")).longValue(),
            (String) row.get("title"),
            (String) row.get("artist"),
            label,
            (String) row.get("description"),
            (BigDecimal) row.get("sleeve_condition"),
            (BigDecimal) row.get("record_condition"),
            (String) row.get("neighborhood"),
            (String) row.get("province"),
            ((Date) row.get("published")).toLocalDate(),
            (BigDecimal) row.get("price")
        );
    }

    @Override
    public Product createProduct(
        final Long userId,
        final String title,
        final String artist,
        final String recordLabel,
        final List<Long> categoryIds,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final String neighborhood,
        final String province,
        final BigDecimal price
    ) {
        final Map<String, Object> values = new HashMap<>();
        final LocalDate published = LocalDate.now();
        final String normalizedLabel = normalizeRecordLabel(recordLabel);

        values.put("user_id", userId);
        values.put("title", title);
        values.put("artist", artist);
        values.put("record_label", normalizedLabel);
        values.put("sleeve_condition", sleeveCondition);
        values.put("record_condition", recordCondition);
        values.put("neighborhood", neighborhood);
        values.put("province", province);
        values.put("price", price);
        values.put("description", description);
        values.put("published", Date.valueOf(published));
        values.put("available", true);

        final Number id = productInsert.executeAndReturnKey(values);
        final Long productId = id.longValue();

        if (categoryIds != null) {
            for (Long categoryId : categoryIds) {
                final Map<String, Object> pcValues = new HashMap<>();
                pcValues.put("product_id", productId);
                pcValues.put("category_id", categoryId);
                productCategoryInsert.execute(pcValues);
            }
        }

        return mapProduct(
            productId, userId, title, artist, normalizedLabel, description,
            sleeveCondition, recordCondition, neighborhood, province, published, price
        );
    }

    @Override
    public List<Product> listProducts() {
        return findProducts(ProductSearchCriteria.empty());
    }

    @Override
    public List<Product> findProducts(final ProductSearchCriteria criteria) {
        final StringBuilder sql = new StringBuilder(
            "SELECT p.product_id, p.user_id, p.title, p.artist, p.record_label, p.description, " +
            "p.sleeve_condition, p.record_condition, p.neighborhood, p.province, p.published, p.price " +
            "FROM products p WHERE p.available = TRUE "
        );
        final List<Object> args = new ArrayList<>();

        if (!criteria.getCategoryIds().isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM products_categories pc WHERE pc.product_id = p.product_id AND pc.category_id IN (");
            sql.append(String.join(",", Collections.nCopies(criteria.getCategoryIds().size(), "?")));
            sql.append(")) ");
            args.addAll(criteria.getCategoryIds());
        }

        if (criteria.getMinPrice() != null) {
            sql.append(" AND p.price >= ? ");
            args.add(criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            sql.append(" AND p.price <= ? ");
            args.add(criteria.getMaxPrice());
        }

        if (!criteria.getRecordLabels().isEmpty()) {
            sql.append(" AND p.record_label IN (");
            sql.append(String.join(",", Collections.nCopies(criteria.getRecordLabels().size(), "?")));
            sql.append(") ");
            args.addAll(criteria.getRecordLabels());
        }

        if (!criteria.getConditionBuckets().isEmpty()) {
            sql.append(" AND (");
            boolean first = true;
            for (ConditionBucket bucket : criteria.getConditionBuckets()) {
                if (!first) {
                    sql.append(" OR ");
                }
                first = false;
                appendConditionBucketSql(sql, bucket);
            }
            sql.append(") ");
        }

        sql.append(" ORDER BY p.published DESC, p.product_id DESC ");

        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return rows.stream().map(this::mapProductFromRow).collect(Collectors.toList());
    }

    @Override
    public List<String> listDistinctRecordLabels() {
        return jdbcTemplate.query(
            "SELECT DISTINCT record_label FROM products WHERE available = TRUE " +
            "AND record_label IS NOT NULL AND TRIM(record_label) <> '' ORDER BY record_label ASC",
            (rs, rowNum) -> rs.getString(1)
        );
    }

    @Override
    public Optional<Product> findById(final Long id) {
        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT product_id, user_id, title, artist, record_label, description, sleeve_condition, record_condition, " +
            "neighborhood, province, published, price FROM products WHERE product_id = ?",
            id
        );

        return rows.stream().findFirst().map(this::mapProductFromRow);
    }

    @Override
    public void markAsSold(final Long id) {
        jdbcTemplate.update("UPDATE products SET available = FALSE WHERE product_id = ?", id);
    }

}
