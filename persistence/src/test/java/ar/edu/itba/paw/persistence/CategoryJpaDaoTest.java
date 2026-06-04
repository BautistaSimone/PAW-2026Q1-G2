package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @PersistenceContext
    private EntityManager em;

    private Category insertCategory(final String name) {
        final Category category = new Category(name);
        em.persist(category);
        return category;
    }

    private User insertUser(final String email, final String username) {
        final User user = new User(
            email,
            "password",
            username,
            false,
            true,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        em.persist(user);
        em.flush();
        return user;
    }

    private Product insertProduct(final Long userId, final String title, final List<Category> categories) {
        final Product product = new Product(
            userId,
            title,
            "Artist",
            "Label",
            "CAT-1",
            "Argentina",
            categories,
            "Desc",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            LocalDate.now(),
            BigDecimal.valueOf(1000),
            1
        );
        em.persist(product);
        em.flush();
        return product;
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
        final User user = insertUser("cat-seller@test.com", "seller");

        final Category rock = insertCategory("Rock");
        final Category ambient = insertCategory("Ambient");
        insertCategory("Jazz");

        final Product product = insertProduct(user.getId(), "Album", List.of(rock, ambient));

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
        final User user = insertUser("cat-empty@test.com", "seller");
        insertCategory("Unlinked");

        final Product product = insertProduct(user.getId(), "Album without categories", Collections.emptyList());

        em.flush();
        em.clear();

        // Act
        final List<Category> categories = categoryDao.findByProductId(product.getId());

        // Assert
        Assertions.assertTrue(categories.isEmpty());
    }
}
