package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Category;

public interface CategoryDao {



    List<Category> findAll();
    List<Category> findByIds(final List<Long> ids);
    List<Category> findByProductId(final Long productId);
    Optional<Category> findById(final Long id);
}
