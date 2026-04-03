package ar.edu.itba.paw.services;

import java.util.List;

import ar.edu.itba.paw.models.Category;

public interface CategoryService {
    List<Category> findAll();
}
