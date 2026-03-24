package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
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
            rs.getString("title"),
            rs.getString("artist"),
            rs.getString("genre"),
            rs.getString("vinyl_condition"),
            rs.getBigDecimal("price"),
            rs.getString("image_url"),
            rs.getString("description"),
            rs.getObject("published", LocalDate.class)
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
        final String title,
        final String artist,
        final String genre,
        final String vinylCondition,
        final BigDecimal price,
        final String imageUrl,
        final String description
    ) {
        final Map<String, Object> values = new HashMap<>();
        final LocalDate publishedDate = LocalDate.now();

        values.put("title", title);
        values.put("artist", artist);
        values.put("genre", genre);
        values.put("vinyl_condition", vinylCondition);
        values.put("price", price);
        values.put("image_url", imageUrl);
        values.put("description", description);
        values.put("published", publishedDate);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new Product(id.longValue(), title, artist, genre, vinylCondition, price, imageUrl, description, publishedDate);
    }

    @Override
    public List<Product> listProducts() {
        return jdbcTemplate.query(
            "SELECT product_id, title, artist, genre, vinyl_condition, price, image_url, description, published " +
                "FROM products ORDER BY published DESC, product_id DESC",
            PRODUCT_ROW_MAPPER
        );
    }
}
