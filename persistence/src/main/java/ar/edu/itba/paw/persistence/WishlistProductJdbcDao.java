package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;

@Repository
public class WishlistProductJdbcDao implements WishlistProductDao {

    private static final RowMapper<Category> CATEGORY_ROW_MAPPER = (rs, rowNum) ->
        new Category(
            rs.getLong("category_id"),
            rs.getString("name")
        );

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (rs, rowNum) ->
        new Product(
            rs.getLong("product_id"),
            rs.getLong("user_id"),
            rs.getString("title"),
            rs.getString("artist"),
            Optional.ofNullable(rs.getString("record_label")).orElse(""),
            Optional.ofNullable(rs.getString("catalog_number")).orElse(""),
            Optional.ofNullable(rs.getString("edition_country")).orElse(""),
            Collections.emptyList(), // FIXME: No categories or check if categories are necessary
            rs.getString("description"),
            rs.getBigDecimal("sleeve_condition"),
            rs.getBigDecimal("record_condition"),
            rs.getDate("published").toLocalDate(),
            rs.getBigDecimal("price")
        );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public WishlistProductJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("wishlist_products");
    }

    @Override
    public void createWishlistProduct(
        final Long productId,
        final Long userId
    ) {
        final Map<String, Object> values = new HashMap<>();

        values.put("product_id", productId);
        values.put("user_id", userId);

        jdbcInsert.execute(values);
    }

    @Override
    public List<Product> findByUserId(final Long userId) {
        // TODO: Check that this works
        return jdbcTemplate.query(
            "SELECT * FROM products p WHERE EXIST " + 
            "(SELECT * FROM wishlist_products wp WHERE " + 
            "wp.user_id = ? AND wp.product_id = p.product_id)",
            PRODUCT_ROW_MAPPER,
            userId
        );
    }
}
