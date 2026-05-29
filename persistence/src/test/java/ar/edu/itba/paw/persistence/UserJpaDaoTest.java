package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Collections;
import java.util.HashSet;
import java.math.BigDecimal;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;

@Rollback   // Clean database before testing
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class UserJpaDaoTest {

    @Autowired
    private UserJpaDao userDao;

    @Autowired
    private ProductJpaDao productDao;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testCreateUserWhenUserDoesNotExist() {
        // Arrange
        final String email = "[EMAIL_ADDRESS]";
        final String password = "[PASSWORD]";
        final String username = "[USERNAME]";
        final Boolean mod = false;
        final Boolean enabled = false;

        // Act
        final User user = userDao.createUser(
                email,
                password,
                username,
                mod,
                enabled,
                "Juan",
                "Perez",
                null,
                null,
                null,
                null,
                null,
                null);

        // Assert
        Assertions.assertNotNull(user);
        Assertions.assertEquals(username, user.getUsername());
        Assertions.assertEquals(password, user.getPassword());

        Long count = em.createQuery(
            "SELECT COUNT(u) FROM User u",
            Long.class
        ).getSingleResult();

        Assertions.assertEquals(1L, count);
    }

    @Test
    public void testAddWishlistProduct() {
        // Arrange
        final String email = "[EMAIL_ADDRESS]";
        final String password = "[PASSWORD]";
        final String username = "[USERNAME]";
        final Boolean mod = false;
        final Boolean enabled = false;

        final User user = userDao.createUser(
                email,
                password,
                username,
                mod,
                enabled,
                "Juan",
                "Perez",
                null,
                null,
                null,
                null,
                null,
                null);

        final Product product = productDao.createProduct(
            user.getId(), "Album", "Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000), 1
        );
        final Product otherProduct = productDao.createProduct(
            user.getId(), "Other Album", "Other Artist", "Label", "CAT2", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000), 1
        );

        // Act
        userDao.addWishlistProduct(user.getId(), product);
        userDao.addWishlistProduct(user.getId(), otherProduct);

        // Assert
        Number count = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM user_wishlist_products wp WHERE wp.user_id = :userId")
            .setParameter("userId", user.getId())
            .getSingleResult();

        Assertions.assertEquals(2L, count.longValue());
    }

    @Test
    public void testRemoveWishlistProduct() {
        // Arrange
        final String email = "[EMAIL_ADDRESS]";
        final String password = "[PASSWORD]";
        final String username = "[USERNAME]";
        final Boolean mod = false;
        final Boolean enabled = false;

        final User user = userDao.createUser(
                email,
                password,
                username,
                mod,
                enabled,
                "Juan",
                "Perez",
                null,
                null,
                null,
                null,
                null,
                null);

        final Product product = productDao.createProduct(
            user.getId(), "Album", "Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000), 1
        );
        final Product otherProduct = productDao.createProduct(
            user.getId(), "Other Album", "Other Artist", "Label", "CAT2", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000), 1
        );

        userDao.addWishlistProduct(user.getId(), product);
        userDao.addWishlistProduct(user.getId(), otherProduct);

        // Act
        userDao.removeWishlistProduct(user.getId(), product);

        // Assert
        Number count = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM user_wishlist_products wp WHERE wp.user_id = :userId")
            .setParameter("userId", user.getId())
            .getSingleResult();

        Assertions.assertEquals(1L, count.longValue());
    }

    @Test
    public void testIsProductInWishlist() {
        // Arrange
        final String email = "[EMAIL_ADDRESS]";
        final String password = "[PASSWORD]";
        final String username = "[USERNAME]";
        final Boolean mod = false;
        final Boolean enabled = false;

        final User user = userDao.createUser(
                email,
                password,
                username,
                mod,
                enabled,
                "Juan",
                "Perez",
                null,
                null,
                null,
                null,
                null,
                null);

        final Product product = productDao.createProduct(
            user.getId(), "Album", "Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Description", BigDecimal.valueOf(8),
            BigDecimal.valueOf(9), BigDecimal.valueOf(1000), 1
        );
        
        userDao.addWishlistProduct(user.getId(), product);

        // Act
        final Boolean isWishlisted = userDao.isProductInWishlist(user.getId(), product.getId());

        // Assert
        Assertions.assertTrue(isWishlisted);
    }
}