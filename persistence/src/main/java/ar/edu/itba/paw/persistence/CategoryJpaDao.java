package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;

@Repository
public class CategoryJpaDao implements CategoryDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Category> findAll() {
        return em.createQuery("FROM Category ORDER BY name ASC", Category.class)
            .getResultList();
    }

    @Override
    public Optional<Category> findById(final Long id) {
        return Optional.ofNullable(em.find(Category.class, id));
    }

    @Override
    public List<Category> findByProductId(final Long productId) {
        final Product product = em.find(Product.class, productId);
        if (product == null) {
            return Collections.emptyList();
        }
        final List<Category> categories = product.getCategories();
        if (categories == null) {
            return Collections.emptyList();
        }
        return categories;
    }
}
