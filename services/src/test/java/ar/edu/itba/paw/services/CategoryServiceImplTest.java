package ar.edu.itba.paw.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.persistence.CategoryDao;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryDao categoryDao;

    @Test
    public void testFindAll() {
        // Arrange
        Category c1 = new Category(1L, "Rock");
        Category c2 = new Category(2L, "Jazz");
        Mockito.when(categoryDao.findAll()).thenReturn(Arrays.asList(c1, c2));

        // Act
        List<Category> result = categoryService.findAll();

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Rock", result.get(0).getName());
        Assertions.assertEquals("Jazz", result.get(1).getName());
    }

    @Test
    public void testFindByIds() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);
        Category c1 = new Category(1L, "Rock");
        Category c2 = new Category(2L, "Jazz");
        Mockito.when(categoryDao.findByIds(ids)).thenReturn(Arrays.asList(c1, c2));

        // Act
        List<Category> result = categoryService.findByIds(ids);

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1L, result.get(0).getId());
        Assertions.assertEquals(2L, result.get(1).getId());
    }

    @Test
    public void testFindByIdExists() {
        // Arrange
        Category c = new Category(1L, "Rock");
        Mockito.when(categoryDao.findById(1L)).thenReturn(Optional.of(c));

        // Act
        Optional<Category> result = categoryService.findById(1L);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Rock", result.get().getName());
    }

    @Test
    public void testFindByIdNotExists() {
        // Arrange
        Mockito.when(categoryDao.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Category> result = categoryService.findById(99L);

        // Assert
        Assertions.assertFalse(result.isPresent());
    }
}
