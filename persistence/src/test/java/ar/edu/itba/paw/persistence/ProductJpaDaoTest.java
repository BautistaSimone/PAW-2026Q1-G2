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

    @Autowired
    private UserJpaDao userDao;

    @PersistenceContext
    private EntityManager em;

    private Product createSuggestionProduct(final User user, final String title,
                                            final String artist, final String recordLabel) {
        return productDao.createProduct(
            user.getId(), title, artist, recordLabel, "CAT-001", "Argentina",
            Collections.emptyList(), "Desc", BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0), BigDecimal.valueOf(1000), 1
        );
    }

    @Test
    public void testCreateProductAllowsMoreThanOneProductPerUser() {

        // Arrange
        final User user = userDao.createUser("seller@test.com", "password", "seller",
            false, true, null, null, null, null, null, null, null, null);

        // Act
        final Product firstProduct = productDao.createProduct(
            user.getId(), "Dynamo", "Soda Stereo", "Sony Music", "EPC 85930", "Argentina",
            Collections.emptyList(), "Edicion original", BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0), BigDecimal.valueOf(32000), 1
        );
        final Product secondProduct = productDao.createProduct(
            user.getId(), "Bocanada", "Gustavo Cerati", "Ariola", "74321 68523-2", "Argentina",
            Collections.emptyList(), "Reedicion 2024", BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(10.0), BigDecimal.valueOf(28000), 1
        );

        // Assert
        Assertions.assertNotNull(firstProduct);
        Assertions.assertNotNull(secondProduct);

        final long count = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
        Assertions.assertEquals(2, count); 
        Assertions.assertEquals(2, productDao.listProducts().getResults().size());
    }

    @Test
    public void findProductsSearchMatchesArtist() {
        // Arrange
        final User user = userDao.createUser("seller2@test.com", "password", "seller2",
            false, true, null, null, null, null, null, null, null, null);
        productDao.createProduct(
            user.getId(), "Bocanada", "Gustavo Cerati", "Ariola", "74321", "Argentina",
            Collections.emptyList(), "Album solista", BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(10.0), BigDecimal.valueOf(28000), 1
        );

        final ProductSearchCriteria criteria = new ProductSearchCriteria(
            "cerati", Collections.emptyList(), null, null,
            Collections.emptyList(), Collections.emptyList(), null, null, 1, 10
        );

        // Act
        final List<Product> found = productDao.findProducts(criteria).getResults();
        
        // Assert
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals("Bocanada", found.get(0).getTitle());
    }

    // @Test
    // public void findProductsSearchReturnsEmptyWhenNoMatch() {
    //     // Arrange
    //     final User user = userDao.createUser("seller3@test.com", "password", "seller3",
    //         false, true, null, null, null, null, null, null, null, null);
    //     productDao.createProduct(
    //         user.getId(), "Dynamo", "Soda Stereo", "Sony", "1", "Argentina",
    //         Collections.emptyList(), "Desc", BigDecimal.valueOf(9.0),
    //         BigDecimal.valueOf(9.0), BigDecimal.valueOf(1000)
    //     );

    //     final ProductSearchCriteria criteria = new ProductSearchCriteria(
    //         "texto_que_no_existe_en_ningun_campo", Collections.emptyList(), null, null,
    //         Collections.emptyList(), Collections.emptyList(), null, null, 1, 10
    //     );

    //     // Act
    //     final List<Product> found = productDao.findProducts(criteria).getResults();

    //     // Assert
    //     Assertions.assertTrue(found.isEmpty());
    // }

    @Test
    public void decrementStockOnlySucceedsWhenStockAvailable() {
        // Arrange
        final User user = userDao.createUser("seller4@test.com", "password", "seller4",
            false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
            user.getId(), "Artaud", "Pescado Rabioso", "Talent", "SE-515", "Argentina",
            Collections.emptyList(), "Original", BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0), BigDecimal.valueOf(45000), 1
        );

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
        final User user = userDao.createUser("seller5@test.com", "password", "seller5",
            false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
            user.getId(), "Album", "Artist", "Label", "CAT-1", "Argentina",
            Collections.emptyList(), "Desc", BigDecimal.valueOf(8.0),
            BigDecimal.valueOf(8.0), BigDecimal.valueOf(1000), 1
        );

        em.flush();

        // Act
        final Boolean first = productDao.markAsUserDeleted(product.getId());
        final Boolean second = productDao.markAsUserDeleted(product.getId());

        // Assert
        Assertions.assertTrue(first);
        Assertions.assertFalse(second);

        Assertions.assertEquals(1, productDao.findProductsByUserIdAndState(
            user.getId(), ProductState.USER_DELETED, 1, 10).getResults().size());
        Assertions.assertTrue(productDao.findByIdIfAvailable(product.getId()).isEmpty());
    }

    @Test
    public void markAsUserDeletedAndRestore() {
        // Arrange
        final User user = userDao.createUser("seller5@test.com", "password", "seller5",
            false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
            user.getId(), "Album", "Artist", "Label", "CAT-1", "Argentina",
            Collections.emptyList(), "Desc", BigDecimal.valueOf(8.0),
            BigDecimal.valueOf(8.0), BigDecimal.valueOf(1000), 1
        );

        em.flush();

        productDao.markAsUserDeleted(product.getId());

        // Act
        final Boolean first = productDao.restoreUserDeletedProduct(product.getId());
        final Boolean second = productDao.restoreUserDeletedProduct(product.getId());

        // Assert
        Assertions.assertTrue(first);
        Assertions.assertFalse(second);
        Assertions.assertTrue(productDao.findByIdIfAvailable(product.getId()).isPresent());
    }

    @Test
    public void decrementStockSetsSoldAtZero() {

        // Arrange
        final User user = userDao.createUser("seller6@test.com", "password", "seller6",
            false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
            user.getId(), "X", "Y", "L", "C", "Argentina",
            Collections.emptyList(), "D", BigDecimal.valueOf(7.0),
            BigDecimal.valueOf(7.0), BigDecimal.valueOf(500), 1
        );

        em.flush();

        // Act
        productDao.decrementStock(product.getId());

        // Assert
        Assertions.assertTrue(productDao.findByIdIfAvailable(product.getId()).isEmpty());
        Assertions.assertTrue(productDao.findById(product.getId()).isPresent());
    }

    @Test
    public void updateProductChangesTitleWhenActive() {
        // Arrange
        final User user = userDao.createUser("seller7@test.com", "password", "seller7",
            false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
            user.getId(), "Old", "Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Desc", BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0), BigDecimal.valueOf(2000), 1
        );

        em.flush();

        // Act
        final Boolean updated = productDao.updateProduct(
            product.getId(), "NewTitle", "Artist", "Label", "CAT", "Argentina",
            Collections.emptyList(), "Desc", BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0), BigDecimal.valueOf(2500), 1
        );

        // Assert
        Assertions.assertTrue(updated);
        final Product reloaded = productDao.findById(product.getId()).orElseThrow();
        Assertions.assertEquals("NewTitle", reloaded.getTitle());
        Assertions.assertEquals(0, reloaded.getPrice().compareTo(BigDecimal.valueOf(2500)));
    }

    @Test
    public void listDistinctArtistsReturnsUniqueVisibleNonBlankArtistsSorted() {
        // Arrange
        final User user = userDao.createUser("seller10@test.com", "password", "seller10",
            false, true, null, null, null, null, null, null, null, null);
        createSuggestionProduct(user, "Visible B", "Zoo Artist", "Label B");
        createSuggestionProduct(user, "Visible A", " Alpha Artist ", "Label A");
        createSuggestionProduct(user, "Duplicate", "Zoo Artist", "Label C");
        createSuggestionProduct(user, "Blank", "   ", "Label D");
        final Product hiddenProduct = createSuggestionProduct(user, "Hidden", "Hidden Artist", "Hidden Label");

        em.flush();
        productDao.markAsUserDeleted(hiddenProduct.getId());

        em.flush();
        em.clear();

        // Act
        final List<String> distinct = productDao.listDistinctArtists();

        // Assert
        Assertions.assertIterableEquals(
            List.of("Alpha Artist", "Zoo Artist"),
            distinct
        );
    }

    @Test
    public void listDistinctRecordLabelsReturnsUniqueVisibleNonBlankLabelsSorted() {

        // Arrange
        final User user = userDao.createUser("seller11@test.com", "password", "seller11",
            false, true, null, null, null, null, null, null, null, null);
        createSuggestionProduct(user, "Visible B", "Artist B", "Zoo Label");
        createSuggestionProduct(user, "Visible A", "Artist A", " Alpha Label ");
        createSuggestionProduct(user, "Duplicate", "Artist C", "Zoo Label");
        createSuggestionProduct(user, "Blank", "Artist D", "   ");
        final Product hiddenProduct = createSuggestionProduct(user, "Hidden", "Artist E", "Hidden Label");

        em.flush();

        productDao.markAsUserDeleted(hiddenProduct.getId());

        em.flush();
        em.clear();

        // Act
        final List<String> distinct = productDao.listDistinctRecordLabels();

        // Assert
        Assertions.assertIterableEquals(
            List.of("Alpha Label", "Zoo Label"),
            distinct
        );
    }

    @Test
    public void suggestArtistsReturnsRankedLimitedUniqueVisibleMatches() {
        // Arrange
        final User user = userDao.createUser("seller12@test.com", "password", "seller12",
            false, true, null, null, null, null, null, null, null, null);
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
        productDao.markAsUserDeleted(hiddenProduct.getId());
        em.flush();
        em.clear();

        // Act
        final List<String> suggestions = productDao.suggestArtists("son", 7);

        // Assert
        Assertions.assertIterableEquals(
            List.of("Son", "Sons", "Sony", "Sonic", "Sonata", "Sonic Youth", "The Sonics"),
            suggestions
        );
    }

    @Test
    public void suggestRecordLabelsReturnsRankedLimitedUniqueVisibleMatches() {
        // Arrange
        final User user = userDao.createUser("seller13@test.com", "password", "seller13",
            false, true, null, null, null, null, null, null, null, null);
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
        productDao.markAsUserDeleted(hiddenProduct.getId());
        em.flush();
        em.clear();

        // Act
        final List<String> suggestions = productDao.suggestRecordLabels("cap", 7);

        // Assert
        Assertions.assertIterableEquals(
            List.of("Cap", "Cape", "Caps", "Capitol", "Capital Records", "Discap", "Blue Cap"),
            suggestions
        );
    }

    @Test
    public void suggestionsEscapeWildcardsAndIgnoreShortQueries() {
        // Arrange
        final User user = userDao.createUser("seller14@test.com", "password", "seller14",
            false, true, null, null, null, null, null, null, null, null);
        createSuggestionProduct(user, "Literal Wildcards", "100%_Artist", "100%_Records");
        createSuggestionProduct(user, "Wildcard Lookalike", "100XAartist", "100XRecords");

        em.flush();
        em.clear();

        // Act & Assert
        Assertions.assertIterableEquals(
            List.of("100%_Artist"),
            productDao.suggestArtists("%_", 7)
        );
        Assertions.assertIterableEquals(
            List.of("100%_Records"),
            productDao.suggestRecordLabels("%_", 7)
        );
        Assertions.assertTrue(productDao.suggestArtists("s", 7).isEmpty());
        Assertions.assertTrue(productDao.suggestRecordLabels("r", 7).isEmpty());
    }
}
