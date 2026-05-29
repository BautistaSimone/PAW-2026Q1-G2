package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class CategoryJpaDaoTest {

    @Autowired
    private CategoryJpaDao categoryDao;

    @Autowired
    private ProductJpaDao productDao;

    @Autowired
    private UserJpaDao userDao;

    @PersistenceContext
    private EntityManager em;

    private Category insertCategory(final String name) {
        final Category category = new Category(name);
        em.persist(category);
        return category;
    }

    @Test
    public void findAllReturnsEveryCategorySortedByName() {

        // Arrange
        insertCategory("Rock");
        insertCategory("Ambient");
        insertCategory("Jazz");

        em.flush();

        // Act
        final List<Category> categories = categoryDao.findAll();

        // Assert
        Assertions.assertEquals(3, categories.size());
        Assertions.assertEquals(
            List.of("Ambient", "Jazz", "Rock"),
            categories.stream().map(Category::getName).toList()
        );
    }

    @Test
    public void findByProductIdReturnsOnlyLinkedCategories() {
        // Arrange
        final User user = userDao.createUser("cat-seller@test.com", "password", "seller",
            false, true, null, null, null, null, null, null, null, null);

        final Category rock = insertCategory("Rock");
        final Category ambient = insertCategory("Ambient");
        insertCategory("Jazz");

        final Product product = productDao.createProduct(
            user.getId(), "Album", "Artist", "Label", "CAT-1", "Argentina",
            List.of(rock, ambient), "Desc", BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0), BigDecimal.valueOf(1000), 1
        );

        em.flush();
        em.clear();

        // Act
        final List<Category> categories = categoryDao.findByProductId(product.getId());

        // Assert
        Assertions.assertEquals(2, categories.size());
    }

    @Test
    public void findByProductIdReturnsEmptyWhenProductHasNoCategories() {
        // Arrange
        final User user = userDao.createUser("cat-empty@test.com", "password", "seller",
            false, true, null, null, null, null, null, null, null, null);
        insertCategory("Unlinked");

        final Product product = productDao.createProduct(
            user.getId(), "Album without categories", "Artist", "Label", "CAT-1", "Argentina",
            Collections.emptyList(), "Desc", BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0), BigDecimal.valueOf(1000), 1
        );

        em.flush();
        em.clear();

        // Act
        final List<Category> categories = categoryDao.findByProductId(product.getId());

        // Assert
        Assertions.assertTrue(categories.isEmpty());
    }
}
