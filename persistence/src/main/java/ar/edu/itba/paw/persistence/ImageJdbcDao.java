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

import ar.edu.itba.paw.models.Image;

@Repository
public class ImageJdbcDao implements ImageDao {

    private static final RowMapper<Image> IMAGE_ROW_MAPPER = (ResultSet rs, int rowNum) ->
        new Image(
            rs.getLong("image_id"),
            rs.getLong("product_id"),
            rs.getBytes("data")
        );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public ImageJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("images");
    }

    @Override
    public Image createImage(
        final Long product_id,
        final byte[] data
    ) {
        final Map<String, Object> values = new HashMap<>();
        final LocalDate published = LocalDate.now();

        values.put("product_id", product_id);
        values.put("data", data);

        // TODO: Do something with the result
        final Number id = jdbcInsert.executeAndReturnKey(values);

        // TODO: Fetch actual User ID
        return new Image(id.longValue(), product_id, data);
    }

    @Override
    public List<Image> listImages() {
        return jdbcTemplate.query(
            "SELECT *" +
                "FROM images ORDER BY product_id DESC",
            IMAGE_ROW_MAPPER
        );
    }
}
