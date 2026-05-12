package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Category;

@Repository
public class CategoryJdbcDao implements CategoryDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Category> findAll() {
        final TypedQuery<Category> query = em.createQuery("FROM Category ORDER BY name ASC", Category.class);
        return query.getResultList();
    }

    @Override
    public List<Category> findByProductId(final Long productId) {
        final TypedQuery<Category> query = em.createQuery("FROM Category WHERE productId = :product_id", Category.class);
        query.setParameter("product_id", productId);
        return query.getResultList();
        return jdbcTemplate.query(
            "SELECT c.category_id, c.name FROM categories c " +
            "JOIN products_categories pc ON c.category_id = pc.category_id " +
            "WHERE pc.product_id = ? ORDER BY c.name ASC",
            CATEGORY_ROW_MAPPER, productId
        );
    }
}
