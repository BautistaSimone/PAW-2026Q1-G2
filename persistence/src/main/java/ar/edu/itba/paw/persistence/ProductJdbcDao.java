package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;

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

    private Product mapProduct(final Long productId, final Long userId, final String title,
                               final String artist, final String description, final BigDecimal sleeveCondition,
                               final BigDecimal recordCondition, final String neighborhood, final String province,
                               final LocalDate published, final BigDecimal price) {
        final List<Category> categories = findCategoriesByProductId(productId);
        return new Product(productId, userId, title, artist, categories, description, sleeveCondition, recordCondition, neighborhood, province, published, price);
    }

    @Override
    public Product createProduct(
        final Long userId,
        final String title,
        final String artist,
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

        values.put("user_id", userId);
        values.put("title", title);
        values.put("artist", artist);
        values.put("sleeve_condition", sleeveCondition);
        values.put("record_condition", recordCondition);
        values.put("neighborhood", neighborhood);
        values.put("province", province);
        values.put("price", price);
        values.put("description", description);
        values.put("published", Date.valueOf(published));

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

        return mapProduct(productId, userId, title, artist, description, sleeveCondition, recordCondition, neighborhood, province, published, price);
    }

    @Override
    public List<Product> listProducts() {
        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT product_id, user_id, title, artist, description, sleeve_condition, record_condition, neighborhood, province, published, price " +
            "FROM products ORDER BY published DESC, product_id DESC"
        );

        return rows.stream().map(row -> mapProduct(
            ((Number) row.get("product_id")).longValue(),
            ((Number) row.get("user_id")).longValue(),
            (String) row.get("title"),
            (String) row.get("artist"),
            (String) row.get("description"),
            (BigDecimal) row.get("sleeve_condition"),
            (BigDecimal) row.get("record_condition"),
            (String) row.get("neighborhood"),
            (String) row.get("province"),
            ((Date) row.get("published")).toLocalDate(),
            (BigDecimal) row.get("price")
        )).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Optional<Product> findById(final Long id) {
        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT product_id, user_id, title, artist, description, sleeve_condition, record_condition, neighborhood, province, published, price " +
            "FROM products WHERE product_id = ?", id
        );

        return rows.stream().findFirst().map(row -> mapProduct(
            ((Number) row.get("product_id")).longValue(),
            ((Number) row.get("user_id")).longValue(),
            (String) row.get("title"),
            (String) row.get("artist"),
            (String) row.get("description"),
            (BigDecimal) row.get("sleeve_condition"),
            (BigDecimal) row.get("record_condition"),
            (String) row.get("neighborhood"),
            (String) row.get("province"),
            ((Date) row.get("published")).toLocalDate(),
            (BigDecimal) row.get("price")
        ));
    }

}

