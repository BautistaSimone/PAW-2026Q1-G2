package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.persistence.CategoryDao;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    @Autowired
    public CategoryServiceImpl(final CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByIds(final List<Long> ids) {
        return categoryDao.findByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(final Long id) {
        return categoryDao.findById(id);
    }
}
