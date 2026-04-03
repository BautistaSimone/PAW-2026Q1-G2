package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Category;

@Repository
public class CategoryJdbcDao implements CategoryDao {

    private static final RowMapper<Category> CATEGORY_ROW_MAPPER = (rs, rowNum) ->
        new Category(
            rs.getLong("category_id"),
            rs.getString("name")
        );

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CategoryJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Category> findAll() {
        return jdbcTemplate.query(
            "SELECT category_id, name FROM categories ORDER BY name ASC",
            CATEGORY_ROW_MAPPER
        );
    }

    @Override
    public List<Category> findByProductId(final Long productId) {
        return jdbcTemplate.query(
            "SELECT c.category_id, c.name FROM categories c " +
            "JOIN products_categories pc ON c.category_id = pc.category_id " +
            "WHERE pc.product_id = ? ORDER BY c.name ASC",
            CATEGORY_ROW_MAPPER, productId
        );
    }
}
