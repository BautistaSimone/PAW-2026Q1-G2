package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Category;

public interface CategoryService {
    List<Category> findAll();
    Optional<Category> findById(final Long id);
}
