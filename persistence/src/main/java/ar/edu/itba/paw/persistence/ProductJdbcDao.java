package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

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
            rs.getObject("published", LocalDate.class),
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
        final String title,
        final String artist,
        final String genre,
        final String description,
        final String condition,
        final BigDecimal price,
        final File image   // TODO: Allow for more than one file
    ) {
        final Map<String, Object> values = new HashMap<>();
        final LocalDate published = LocalDate.now();

        values.put("title", title);
        values.put("artist", artist);
        values.put("genre", genre);
        values.put("condition", condition);
        values.put("price", price);
        // values.put("image_url", imageUrl); // TODO: Implement image creation and storage in DB images table
        values.put("description", description);
        values.put("published", published);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        // TODO: Fetch actual User ID
        return new Product(id.longValue(), 1L, title, artist, genre, description, condition, published, price);
    }

    // TODO: Actualizar al schema
    @Override
    public List<Product> listProducts() {
    //     return jdbcTemplate.query(
    //         "SELECT product_id, title, artist, genre, vinyl_condition, price, image_url, description, published " +
    //             "FROM products ORDER BY published DESC, product_id DESC",
    //         PRODUCT_ROW_MAPPER
    //     );
        return List.of();
    }
}
