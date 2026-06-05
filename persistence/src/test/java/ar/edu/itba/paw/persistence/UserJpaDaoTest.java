package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductState;

@Rollback // Clean database before testing
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class UserJpaDaoTest {

    @Autowired
    private UserJpaDao userDao;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    public void createFollowTable() {
        em.createNativeQuery(
                "CREATE TABLE IF NOT EXISTS user_follows (" +
                        "follower_id INTEGER NOT NULL, " +
                        "followed_id INTEGER NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                        "PRIMARY KEY (follower_id, followed_id))")
                .executeUpdate();
    }

    private User createUser(final String username) {
        final User user = new User(
            username + "@test.com",
            "password",
            username,
            false,
            true,
            false,
            "Juan",
            "Perez",
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

    private Product createProduct(final User user, final String title) {
        final Product product = new Product(
            user.getId(),
            title,
            "Artist",
            "Label",
            "CAT-" + title,
            "Argentina",
            Collections.emptyList(),
            "Description",
            BigDecimal.valueOf(8),
            BigDecimal.valueOf(9),
            LocalDate.now(),
            BigDecimal.valueOf(1000),
            1
        );
        em.persist(product);
        em.flush();
        return product;
    }

    private void follow(final User follower, final User followed) {
        em.createNativeQuery("INSERT INTO user_follows (follower_id, followed_id) VALUES (:followerId, :followedId)")
                .setParameter("followerId", follower.getId())
                .setParameter("followedId", followed.getId())
                .executeUpdate();
    }

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
            null
        );

        // Assert
        Assertions.assertNotNull(user);
        Assertions.assertEquals(username, user.getUsername());
        Assertions.assertEquals(password, user.getPassword());

        Long count = em.createQuery(
                "SELECT COUNT(u) FROM User u",
                Long.class).getSingleResult();

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

        final User user = new User(
            email,
            password,
            username,
            mod,
            enabled,
            false,
            "Juan",
            "Perez",
            null,
            null,
            null,
            null,
            null,
            null
        );
        em.persist(user);
        em.flush();

        final Product product = createProduct(user, "Album");
        final Product otherProduct = createProduct(user, "Other Album");

        // Act
        userDao.addWishlistProduct(user.getId(), product);
        userDao.addWishlistProduct(user.getId(), otherProduct);

        // Assert
        em.flush();

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

        final User user = new User(
            email,
            password,
            username,
            mod,
            enabled,
            false,
            "Juan",
            "Perez",
            null,
            null,
            null,
            null,
            null,
            null
        );
        em.persist(user);
        em.flush();

        final Product product = createProduct(user, "Album");
        final Product otherProduct = createProduct(user, "Other Album");

        user.getWishlistProducts().add(product);
        user.getWishlistProducts().add(otherProduct);
        em.flush();

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

        final User user = new User(
            email,
            password,
            username,
            mod,
            enabled,
            false,
            "Juan",
            "Perez",
            null,
            null,
            null,
            null,
            null,
            null
        );
        em.persist(user);
        em.flush();

        final Product product = createProduct(user, "Album");
        
        user.getWishlistProducts().add(product);
        em.flush();

        // Act
        final boolean isWishlisted = userDao.isProductInWishlist(user.getId(), product.getId());

        // Assert
        Assertions.assertTrue(isWishlisted);
    }

    @Test
    public void getFeaturedActiveSellersExcludesHiddenAndOrdersByFollowersThenProducts() {
        // Arrange
        final User sellerA = createUser("seller_a");
        final User sellerB = createUser("seller_b");
        final User sellerC = createUser("seller_c");
        final User bannedSeller = createUser("banned_seller");
        final User noProducts = createUser("no_products");
        final User hiddenOnly = createUser("hidden_only");
        final User follower1 = createUser("follower_1");
        final User follower2 = createUser("follower_2");
        final User follower3 = createUser("follower_3");

        createProduct(sellerA, "A1");
        createProduct(sellerB, "B1");
        createProduct(sellerB, "B2");
        createProduct(sellerC, "C1");
        createProduct(bannedSeller, "Banned1");
        final Product hiddenProduct = createProduct(hiddenOnly, "Hidden1");
        hiddenProduct.setState(ProductState.USER_DELETED);
        em.flush();

        em.createQuery("UPDATE User u SET u.banned = true WHERE u.id = :userId")
            .setParameter("userId", bannedSeller.getId())
            .executeUpdate();

        follow(follower1, sellerA);
        follow(follower2, sellerA);
        follow(follower1, sellerB);
        follow(follower2, sellerB);
        follow(follower1, sellerC);
        follow(follower2, sellerC);
        follow(follower3, sellerC);
        follow(follower3, bannedSeller);

        em.flush();
        em.clear();

        // Act
        final List<Long> resultIds = userDao.getFeaturedActiveSellers(1, 10)
                .getResults()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // Assert
        Assertions.assertIterableEquals(List.of(sellerC.getId(), sellerB.getId(), sellerA.getId()), resultIds);
        Assertions.assertFalse(resultIds.contains(bannedSeller.getId()));
        Assertions.assertFalse(resultIds.contains(noProducts.getId()));
        Assertions.assertFalse(resultIds.contains(hiddenOnly.getId()));
    }

    @Test
    public void searchActiveSellersEscapesLikeWildcards() {
        // Arrange
        final User literal = createUser("literal%_seller");
        final User wildcardLookalike = createUser("literalXXseller");
        createProduct(literal, "Literal");
        createProduct(wildcardLookalike, "Wildcard");

        em.flush();
        em.clear();

        // Act
        final List<User> results = userDao.searchActiveSellers("%_", 1, 10).getResults();

        // Assert
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(literal.getId(), results.get(0).getId());
    }

    @Test
    public void bulkFollowerCountsAndStatusesReturnDefaults() {
        // Arrange
        final User sellerA = createUser("bulk_seller_a");
        final User sellerB = createUser("bulk_seller_b");
        final User follower1 = createUser("bulk_follower_1");
        final User follower2 = createUser("bulk_follower_2");

        follow(follower1, sellerA);
        follow(follower2, sellerA);
        follow(follower1, sellerB);

        em.flush();
        em.clear();

        // Act
        final Map<Long, Long> counts = userDao
                .countFollowersByUserIds(List.of(sellerA.getId(), sellerB.getId(), follower2.getId()));
        final Map<Long, Boolean> statuses = userDao.followingStatusByUserIds(follower1.getId(),
                List.of(sellerA.getId(), sellerB.getId(), follower2.getId()));

        // Assert
        Assertions.assertEquals(2L, counts.get(sellerA.getId()));
        Assertions.assertEquals(1L, counts.get(sellerB.getId()));
        Assertions.assertEquals(0L, counts.get(follower2.getId()));
        Assertions.assertTrue(statuses.get(sellerA.getId()));
        Assertions.assertTrue(statuses.get(sellerB.getId()));
        Assertions.assertFalse(statuses.get(follower2.getId()));
    }
}
