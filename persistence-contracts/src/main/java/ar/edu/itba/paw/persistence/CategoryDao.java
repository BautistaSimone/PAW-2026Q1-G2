package ar.edu.itba.paw.persistence;

import java.util.List;

import ar.edu.itba.paw.models.Category;

public interface CategoryDao {
    List<Category> findAll();

    List<Category> findByProductId(final Long productId);
}
