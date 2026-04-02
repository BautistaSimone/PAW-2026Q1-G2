package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Product;

@Repository
public class ProductJdbcDao implements ProductDao {

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (ResultSet rs, int rowNum) ->
        new Product(
            rs.getLong("product_id"),
            rs.getLong("user_id"),
            rs.getString("title"),
            rs.getString("artist"),
            rs.getString("genre"),
            rs.getString("description"),
            rs.getString("condition"),
            rs.getDate("published").toLocalDate(),
            rs.getBigDecimal("price")
        );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public ProductJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("products")
            .usingGeneratedKeyColumns("product_id");
    }

    @Override
    public Product createProduct(
        final Long userId,
        final String title,
        final String artist,
        final String genre,
        final String description,
        final String condition,
        final BigDecimal price
    ) {
        final Map<String, Object> values = new HashMap<>();
        final LocalDate published = LocalDate.now();

        values.put("user_id", userId);
        values.put("title", title);
        values.put("artist", artist);
        values.put("genre", genre);
        values.put("condition", condition);
        values.put("price", price);
        values.put("description", description);
        values.put("published", Date.valueOf(published));

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new Product(id.longValue(), userId, title, artist, genre, description, condition, published, price);
    }

    @Override
    public List<Product> listProducts() {
        return jdbcTemplate.query(
            "SELECT product_id, user_id, title, artist, genre, description, condition, published, price " +
                "FROM products ORDER BY published DESC, product_id DESC",
            PRODUCT_ROW_MAPPER
        );
    }
}
