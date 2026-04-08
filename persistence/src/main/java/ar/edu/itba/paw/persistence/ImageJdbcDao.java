package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            rs.getBytes("data"),
            rs.getString("content_type")
        );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public ImageJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("images")
            .usingGeneratedKeyColumns("image_id");
    }

    @Override
    public Image createImage(
        final Long productId,
        final byte[] data,
        final String contentType
    ) {
        final Map<String, Object> values = new HashMap<>();

        values.put("product_id", productId);
        values.put("data", data);
        values.put("content_type", contentType);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new Image(id.longValue(), productId, data, contentType);
    }

    @Override
    public Optional<Image> findById(final Long imageId) {
        return jdbcTemplate.query(
            "SELECT image_id, product_id, data, content_type FROM images WHERE image_id = ?",
            IMAGE_ROW_MAPPER,
            imageId
        ).stream().findFirst();
    }

    @Override
    public Optional<Image> findByProductId(final Long productId) {
        return jdbcTemplate.query(
            "SELECT image_id, product_id, data, content_type FROM images WHERE product_id = ? ORDER BY image_id ASC LIMIT 1",
            IMAGE_ROW_MAPPER,
            productId
        ).stream().findFirst();
    }

    @Override
    public List<Image> findAllByProductId(final Long productId) {
        return jdbcTemplate.query(
            "SELECT image_id, product_id, data, content_type FROM images WHERE product_id = ? ORDER BY image_id ASC",
            IMAGE_ROW_MAPPER,
            productId
        );
    }

    @Override
    public boolean existsByProductId(final Long productId) {
        final Integer imageCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM images WHERE product_id = ?",
            Integer.class,
            productId
        );

        return imageCount != null && imageCount > 0;
    }

    @Override
    public List<Image> listImages() {
        return jdbcTemplate.query(
            "SELECT image_id, product_id, data, content_type FROM images ORDER BY product_id DESC, image_id DESC",
            IMAGE_ROW_MAPPER
        );
    }
}
