package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductState;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ProductJpaDaoTest {

    @Autowired
    private ProductJpaDao productDao;

    @PersistenceContext
    private EntityManager em;

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
                null);
        em.persist(user);
        em.flush();
        return user;
    }

    private Product insertProduct(
            final Long userId,
            final String title,
            final String artist,
            final String recordLabel,
            final String catalogNumber,
            final String editionCountry,
            final List<Category> categories,
            final String description,
            final BigDecimal sleeveCondition,
            final BigDecimal recordCondition,
            final BigDecimal price,
            final int stock) {
        final Product product = new Product(
                userId,
                title,
                artist,
                recordLabel,
                catalogNumber,
                editionCountry,
                categories,
                description,
                sleeveCondition,
                recordCondition,
                LocalDate.now(),
                price,
                stock);
        em.persist(product);
        em.flush();
        return product;
    }

    private void setProductState(final Product product, final ProductState state) {
        product.setState(state);
        em.flush();
    }

    private Product createSuggestionProduct(final User user, final String title,
            final String artist, final String recordLabel) {
        return insertProduct(
                user.getId(),
                title,
                artist,
                recordLabel,
                "CAT-001",
                "Argentina",
                Collections.emptyList(),
                "Desc",
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(1000),
                1);
    }

    @Test
    public void testCreateProductAllowsMoreThanOneProductPerUser() {

        // Arrange
        final User user = insertUser("seller@test.com", "seller");

        // Act
        final Product firstProduct = productDao.createProduct(
                user.getId(), "Dynamo", "Soda Stereo", "Sony Music", "EPC 85930", "Argentina",
                Collections.emptyList(), "Edicion original", BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(9.0), BigDecimal.valueOf(32000), 1);
        final Product secondProduct = productDao.createProduct(
                user.getId(), "Bocanada", "Gustavo Cerati", "Ariola", "74321 68523-2", "Argentina",
                Collections.emptyList(), "Reedicion 2024", BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(10.0), BigDecimal.valueOf(28000), 1);

        // Assert
        Assertions.assertNotNull(firstProduct);
        Assertions.assertNotNull(secondProduct);

        em.flush();
        em.clear();

        final long count = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
        Assertions.assertEquals(2, count);
    }

    @Test
    public void findProductsSearchMatchesArtist() {
        // Arrange
        final User user = insertUser("seller2@test.com", "seller2");
        insertProduct(
                user.getId(),
                "Bocanada",
                "Gustavo Cerati",
                "Ariola",
                "74321",
                "Argentina",
                Collections.emptyList(),
                "Album solista",
                BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(28000),
                1);

        final ProductSearchCriteria criteria = new ProductSearchCriteria(
                "cerati", Collections.emptyList(), null, null,
                Collections.emptyList(), Collections.emptyList(), null, null, 1, 10);

        // Act
        final List<Product> found = productDao.findProducts(criteria).getResults();

        // Assert
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals("Bocanada", found.get(0).getTitle());
    }

    // @Test
    // public void findProductsSearchReturnsEmptyWhenNoMatch() {
    // // Arrange
    // final User user = userDao.createUser("seller3@test.com", "password",
    // "seller3",
    // false, true, null, null, null, null, null, null, null, null);
    // productDao.createProduct(
    // user.getId(), "Dynamo", "Soda Stereo", "Sony", "1", "Argentina",
    // Collections.emptyList(), "Desc", BigDecimal.valueOf(9.0),
    // BigDecimal.valueOf(9.0), BigDecimal.valueOf(1000)
    // );

    // final ProductSearchCriteria criteria = new ProductSearchCriteria(
    // "texto_que_no_existe_en_ningun_campo", Collections.emptyList(), null, null,
    // Collections.emptyList(), Collections.emptyList(), null, null, 1, 10
    // );

    // // Act
    // final List<Product> found = productDao.findProducts(criteria).getResults();

    // // Assert
    // Assertions.assertTrue(found.isEmpty());
    // }

    @Test
    public void decrementStockOnlySucceedsWhenStockAvailable() {
        // Arrange
        final User user = insertUser("seller4@test.com", "seller4");
        final Product product = insertProduct(
                user.getId(),
                "Artaud",
                "Pescado Rabioso",
                "Talent",
                "SE-515",
                "Argentina",
                Collections.emptyList(),
                "Original",
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(45000),
                1);

        em.flush();

        // Act
        final Boolean first = productDao.decrementStock(product.getId());
        final Boolean second = productDao.decrementStock(product.getId());

        // Assert
        Assertions.assertTrue(first);
        Assertions.assertFalse(second);
    }

    @Test
    public void markAsUserDeleted() {
        // Arrange
        final User user = insertUser("seller5@test.com", "seller5");
        final Product product = insertProduct(
                user.getId(),
                "Album",
                "Artist",
                "Label",
                "CAT-1",
                "Argentina",
                Collections.emptyList(),
                "Desc",
                BigDecimal.valueOf(8.0),
                BigDecimal.valueOf(8.0),
                BigDecimal.valueOf(1000),
                1);

        em.flush();

        // Act
        final Boolean first = productDao.markAsUserDeleted(product.getId());
        final Boolean second = productDao.markAsUserDeleted(product.getId());

        // Assert
        Assertions.assertTrue(first);
        Assertions.assertFalse(second);

        em.flush();
        em.clear();

        final ProductState state = em.createQuery(
                "SELECT p.state FROM Product p WHERE p.productId = :productId",
                ProductState.class).setParameter("productId", product.getId()).getSingleResult();
        Assertions.assertEquals(ProductState.USER_DELETED, state);
    }

    @Test
    public void markAsUserDeletedAndRestore() {
        // Arrange
        final User user = insertUser("seller5@test.com", "seller5");
        final Product product = insertProduct(
                user.getId(),
                "Album",
                "Artist",
                "Label",
                "CAT-1",
                "Argentina",
                Collections.emptyList(),
                "Desc",
                BigDecimal.valueOf(8.0),
                BigDecimal.valueOf(8.0),
                BigDecimal.valueOf(1000),
                1);

        em.flush();

        productDao.markAsUserDeleted(product.getId());

        // Act
        final Boolean first = productDao.restoreUserDeletedProduct(product.getId());
        final Boolean second = productDao.restoreUserDeletedProduct(product.getId());

        // Assert
        Assertions.assertTrue(first);
        Assertions.assertFalse(second);
        em.flush();
        em.clear();

        final ProductState state = em.createQuery(
                "SELECT p.state FROM Product p WHERE p.productId = :productId",
                ProductState.class).setParameter("productId", product.getId()).getSingleResult();
        Assertions.assertEquals(ProductState.ACTIVE, state);
    }

    @Test
    public void decrementStockSetsSoldAtZero() {

        // Arrange
        final User user = insertUser("seller6@test.com", "seller6");
        final Product product = insertProduct(
                user.getId(),
                "X",
                "Y",
                "L",
                "C",
                "Argentina",
                Collections.emptyList(),
                "D",
                BigDecimal.valueOf(7.0),
                BigDecimal.valueOf(7.0),
                BigDecimal.valueOf(500),
                1);

        em.flush();

        // Act
        productDao.decrementStock(product.getId());

        // Assert
        em.flush();
        em.clear();

        final Product reloaded = em.find(Product.class, product.getId());
        Assertions.assertEquals(ProductState.SOLD, reloaded.getState());
        Assertions.assertEquals(0, reloaded.getStock());
    }

    @Test
    public void updateProductChangesTitleWhenActive() {
        // Arrange
        final User user = insertUser("seller7@test.com", "seller7");
        final Product product = insertProduct(
                user.getId(),
                "Old",
                "Artist",
                "Label",
                "CAT",
                "Argentina",
                Collections.emptyList(),
                "Desc",
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(2000),
                1);

        em.flush();

        // Act
        final Boolean updated = productDao.updateProduct(
                product.getId(), "NewTitle", "Artist", "Label", "CAT", "Argentina",
                Collections.emptyList(), "Desc", BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(9.0), BigDecimal.valueOf(2500), 1);

        // Assert
        Assertions.assertTrue(updated);
        em.flush();
        em.clear();

        final Product reloaded = em.find(Product.class, product.getId());
        Assertions.assertEquals("NewTitle", reloaded.getTitle());
        Assertions.assertEquals(0, reloaded.getPrice().compareTo(BigDecimal.valueOf(2500)));
    }

    @Test
    public void listDistinctArtistsReturnsUniqueVisibleNonBlankArtistsSorted() {
        // Arrange
        final User user = insertUser("seller10@test.com", "seller10");
        createSuggestionProduct(user, "Visible B", "Zoo Artist", "Label B");
        createSuggestionProduct(user, "Visible A", " Alpha Artist ", "Label A");
        createSuggestionProduct(user, "Duplicate", "Zoo Artist", "Label C");
        createSuggestionProduct(user, "Blank", "   ", "Label D");
        final Product hiddenProduct = createSuggestionProduct(user, "Hidden", "Hidden Artist", "Hidden Label");

        em.flush();
        setProductState(hiddenProduct, ProductState.USER_DELETED);

        em.flush();
        em.clear();

        // Act
        final List<String> distinct = productDao.listDistinctArtists();

        // Assert
        Assertions.assertIterableEquals(
                List.of("Alpha Artist", "Zoo Artist"),
                distinct);
    }

    @Test
    public void listDistinctRecordLabelsReturnsUniqueVisibleNonBlankLabelsSorted() {

        // Arrange
        final User user = insertUser("seller11@test.com", "seller11");
        createSuggestionProduct(user, "Visible B", "Artist B", "Zoo Label");
        createSuggestionProduct(user, "Visible A", "Artist A", " Alpha Label ");
        createSuggestionProduct(user, "Duplicate", "Artist C", "Zoo Label");
        createSuggestionProduct(user, "Blank", "Artist D", "   ");
        final Product hiddenProduct = createSuggestionProduct(user, "Hidden", "Artist E", "Hidden Label");

        em.flush();
        setProductState(hiddenProduct, ProductState.USER_DELETED);

        em.flush();
        em.clear();

        // Act
        final List<String> distinct = productDao.listDistinctRecordLabels();

        // Assert
        Assertions.assertIterableEquals(
                List.of("Alpha Label", "Zoo Label"),
                distinct);
    }

    @Test
    public void suggestArtistsReturnsRankedLimitedUniqueVisibleMatches() {
        // Arrange
        final User user = insertUser("seller12@test.com", "seller12");
        createSuggestionProduct(user, "Exact", " Son ", "Label A");
        createSuggestionProduct(user, "Prefix 1", "Sons", "Label B");
        createSuggestionProduct(user, "Prefix 2", "Sony", "Label C");
        createSuggestionProduct(user, "Prefix 3", "Sonic", "Label D");
        createSuggestionProduct(user, "Prefix 4", "Sonata", "Label E");
        createSuggestionProduct(user, "Prefix 5", "Sonic Youth", "Label F");
        createSuggestionProduct(user, "Contains 1", "The Sonics", "Label G");
        createSuggestionProduct(user, "Contains 2", "Awesome Son", "Label H");
        createSuggestionProduct(user, "Duplicate", "Sonic", "Label I");
        createSuggestionProduct(user, "Blank", "   ", "Label J");
        createSuggestionProduct(user, "No Match", "Cerati", "Label K");
        final Product hiddenProduct = createSuggestionProduct(user, "Hidden", "Son Hidden", "Hidden Label");

        em.flush();
        setProductState(hiddenProduct, ProductState.USER_DELETED);
        em.flush();
        em.clear();

        // Act
        final List<String> suggestions = productDao.suggestArtists("son", 7);

        // Assert
        Assertions.assertIterableEquals(
                List.of("Son", "Sons", "Sony", "Sonic", "Sonata", "Sonic Youth", "The Sonics"),
                suggestions);
    }

    @Test
    public void suggestRecordLabelsReturnsRankedLimitedUniqueVisibleMatches() {
        // Arrange
        final User user = insertUser("seller13@test.com", "seller13");
        createSuggestionProduct(user, "Exact", "Artist A", " Cap ");
        createSuggestionProduct(user, "Prefix 1", "Artist B", "Cape");
        createSuggestionProduct(user, "Prefix 2", "Artist C", "Caps");
        createSuggestionProduct(user, "Prefix 3", "Artist D", "Capitol");
        createSuggestionProduct(user, "Prefix 4", "Artist E", "Capital Records");
        createSuggestionProduct(user, "Contains 1", "Artist F", "Discap");
        createSuggestionProduct(user, "Contains 2", "Artist G", "Blue Cap");
        createSuggestionProduct(user, "Contains 3", "Artist H", "Late Cap");
        createSuggestionProduct(user, "Duplicate", "Artist I", "Capitol");
        createSuggestionProduct(user, "Blank", "Artist J", "   ");
        createSuggestionProduct(user, "No Match", "Artist K", "Ariola");
        final Product hiddenProduct = createSuggestionProduct(user, "Hidden", "Artist L", "Cap Hidden");

        em.flush();
        setProductState(hiddenProduct, ProductState.USER_DELETED);
        em.flush();
        em.clear();

        // Act
        final List<String> suggestions = productDao.suggestRecordLabels("cap", 7);

        // Assert
        Assertions.assertIterableEquals(
                List.of("Cap", "Cape", "Caps", "Capitol", "Capital Records", "Discap", "Blue Cap"),
                suggestions);
    }

    @Test
    public void suggestionsEscapeWildcardsAndIgnoreShortQueries() {
        // Arrange
        final User user = insertUser("seller14@test.com", "seller14");
        createSuggestionProduct(user, "Literal Wildcards", "100%_Artist", "100%_Records");
        createSuggestionProduct(user, "Wildcard Lookalike", "100XAartist", "100XRecords");

        em.flush();
        em.clear();

        // Act & Assert
        Assertions.assertIterableEquals(
                List.of("100%_Artist"),
                productDao.suggestArtists("%_", 7));
        Assertions.assertIterableEquals(
                List.of("100%_Records"),
                productDao.suggestRecordLabels("%_", 7));
        Assertions.assertTrue(productDao.suggestArtists("s", 7).isEmpty());
        Assertions.assertTrue(productDao.suggestRecordLabels("r", 7).isEmpty());
    }

    @Test
    public void findActiveProductsByUserIdReturnsOnlyActiveProductsPaginatedAndSorted() {
        // Arrange
        final User user = insertUser("active-seller@test.com", "active_seller");
        final Product first = createSuggestionProduct(user, "First", "Artist", "Label");
        final Product hidden = createSuggestionProduct(user, "Hidden", "Artist", "Label");
        final Product latest = createSuggestionProduct(user, "Latest", "Artist", "Label");
        setProductState(hidden, ProductState.USER_DELETED);

        em.flush();
        em.clear();

        // Act
        final var firstPage = productDao.findActiveProductsByUserId(user.getId(), 1, 1);
        final var secondPage = productDao.findActiveProductsByUserId(user.getId(), 2, 1);

        // Assert
        Assertions.assertEquals(2L, firstPage.getTotalCount());
        Assertions.assertEquals(latest.getId(), firstPage.getResults().get(0).getId());
        Assertions.assertEquals(first.getId(), secondPage.getResults().get(0).getId());
    }

    @Test
    public void countActiveProductsByUserIdsReturnsZeroForMissingUsers() {
        // Arrange
        final User userA = insertUser("count-a@test.com", "count_a");
        final User userB = insertUser("count-b@test.com", "count_b");
        final User userC = insertUser("count-c@test.com", "count_c");

        createSuggestionProduct(userA, "A1", "Artist", "Label");
        final Product hidden = createSuggestionProduct(userA, "A2", "Artist", "Label");
        setProductState(hidden, ProductState.USER_DELETED);
        createSuggestionProduct(userB, "B1", "Artist", "Label");

        em.flush();
        em.clear();

        // Act
        final Map<Long, Long> counts = productDao
                .countActiveProductsByUserIds(List.of(userA.getId(), userB.getId(), userC.getId()));

        // Assert
        Assertions.assertEquals(1L, counts.get(userA.getId()));
        Assertions.assertEquals(1L, counts.get(userB.getId()));
        Assertions.assertEquals(0L, counts.get(userC.getId()));
    }

    @Test
    public void findLatestActiveProductsByUserIdsLimitsEachUserAndSkipsHidden() {
        // Arrange
        final User userA = insertUser("latest-a@test.com", "latest_a");
        final User userB = insertUser("latest-b@test.com", "latest_b");

        final Product first = createSuggestionProduct(userA, "A1", "Artist", "Label");
        final Product hidden = createSuggestionProduct(userA, "A2", "Artist", "Label");
        final Product latest = createSuggestionProduct(userA, "A3", "Artist", "Label");
        final Product onlyB = createSuggestionProduct(userB, "B1", "Artist", "Label");
        setProductState(hidden, ProductState.USER_DELETED);

        em.flush();
        em.clear();

        // Act
        final Map<Long, List<Product>> productsByUser = productDao.findLatestActiveProductsByUserIds(
                List.of(userA.getId(), userB.getId()),
                2);
        final List<Long> userAProductIds = productsByUser.get(userA.getId()).stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // Assert
        Assertions.assertIterableEquals(List.of(latest.getId(), first.getId()), userAProductIds);
        Assertions.assertEquals(1, productsByUser.get(userB.getId()).size());
        Assertions.assertEquals(onlyB.getId(), productsByUser.get(userB.getId()).get(0).getId());
    }

    @Test
    public void markAllAsAdminHiddenByUserIdOnlyHidesActiveProductsOfTargetUser() {
        // Arrange
        final User targetUser = insertUser("ban-target@test.com", "ban_target");
        final User otherUser = insertUser("ban-other@test.com", "ban_other");

        // 3 active products for target user
        final Product active1 = createSuggestionProduct(targetUser, "Active1", "Artist", "Label");
        final Product active2 = createSuggestionProduct(targetUser, "Active2", "Artist", "Label");
        final Product active3 = createSuggestionProduct(targetUser, "Active3", "Artist", "Label");

        // 1 sold product for target user (should NOT be affected)
        final Product sold = createSuggestionProduct(targetUser, "Sold", "Artist", "Label");
        sold.setStock(0);
        setProductState(sold, ProductState.SOLD);

        // 1 active product for another user (should NOT be affected)
        final Product otherProduct = createSuggestionProduct(otherUser, "Other", "Artist", "Label");

        em.flush();
        em.clear();

        // Act
        final int affected = productDao.markAllAsAdminHiddenByUserId(targetUser.getId());

        em.flush();
        em.clear();

        // Assert
        Assertions.assertEquals(3, affected);

        // Target user's active products are now ADMIN_HIDDEN
        final ProductState active1State = em.createQuery(
                "SELECT p.state FROM Product p WHERE p.productId = :productId",
                ProductState.class).setParameter("productId", active1.getId()).getSingleResult();
        final ProductState active2State = em.createQuery(
                "SELECT p.state FROM Product p WHERE p.productId = :productId",
                ProductState.class).setParameter("productId", active2.getId()).getSingleResult();
        final ProductState active3State = em.createQuery(
                "SELECT p.state FROM Product p WHERE p.productId = :productId",
                ProductState.class).setParameter("productId", active3.getId()).getSingleResult();
        Assertions.assertEquals(ProductState.ADMIN_HIDDEN, active1State);
        Assertions.assertEquals(ProductState.ADMIN_HIDDEN, active2State);
        Assertions.assertEquals(ProductState.ADMIN_HIDDEN, active3State);

        // Target user's SOLD product was NOT affected
        final ProductState soldState = em.createQuery(
                "SELECT p.state FROM Product p WHERE p.productId = :productId",
                ProductState.class).setParameter("productId", sold.getId()).getSingleResult();
        Assertions.assertEquals(ProductState.SOLD, soldState);

        // Other user's product was NOT affected
        final ProductState otherState = em.createQuery(
                "SELECT p.state FROM Product p WHERE p.productId = :productId",
                ProductState.class).setParameter("productId", otherProduct.getId()).getSingleResult();
        Assertions.assertEquals(ProductState.ACTIVE, otherState);
    }
}
