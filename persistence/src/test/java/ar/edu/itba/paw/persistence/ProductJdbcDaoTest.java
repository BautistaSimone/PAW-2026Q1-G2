package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductState;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ProductJdbcDaoTest {

    @Autowired
    private ProductJdbcDao productDao;

    @Autowired
    private UserJdbcDao userDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void testCreateProductAllowsMoreThanOneProductPerUser() {
        final User user = userDao.createUser("seller@test.com", "password", "seller", false, true, null, null, null, null, null, null, null, null);

        final Product firstProduct = productDao.createProduct(
            user.getId(),
            "Dynamo",
            "Soda Stereo",
            "Sony Music",
            "EPC 85930",
            "Argentina",
            Collections.emptyList(),
            "Edicion original",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(32000)
        );
        final Product secondProduct = productDao.createProduct(
            user.getId(),
            "Bocanada",
            "Gustavo Cerati",
            "Ariola",
            "74321 68523-2",
            "Argentina",
            Collections.emptyList(),
            "Reedicion 2024",
            BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(28000)
        );

        Assertions.assertNotNull(firstProduct);
        Assertions.assertNotNull(secondProduct);
        Assertions.assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "products"));
        Assertions.assertEquals(2, productDao.listProducts().getResults().size());
    }

    @Test
    public void findProductsSearchMatchesArtist() {
        final User user = userDao.createUser("seller2@test.com", "password", "seller2", false, true, null, null, null, null, null, null, null, null);
        productDao.createProduct(
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
            BigDecimal.valueOf(28000)
        );

        final ProductSearchCriteria criteria = new ProductSearchCriteria(
            "cerati",
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            null,
            1,
            10
        );
        final List<Product> found = productDao.findProducts(criteria).getResults();
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals("Bocanada", found.get(0).getTitle());
    }

    @Test
    public void findProductsSearchReturnsEmptyWhenNoMatch() {
        final User user = userDao.createUser("seller3@test.com", "password", "seller3", false, true, null, null, null, null, null, null, null, null);
        productDao.createProduct(
            user.getId(),
            "Dynamo",
            "Soda Stereo",
            "Sony",
            "1",
            "Argentina",
            Collections.emptyList(),
            "Desc",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(1000)
        );

        final ProductSearchCriteria criteria = new ProductSearchCriteria(
            "texto_que_no_existe_en_ningun_campo",
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            null,
            1,
            10
        );
        Assertions.assertTrue(productDao.findProducts(criteria).getResults().isEmpty());
    }

    @Test
    public void reserveIfAvailableHidesProductAndOnlySucceedsOnce() {
        final User user = userDao.createUser("seller4@test.com", "password", "seller4", false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
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
            BigDecimal.valueOf(45000)
        );

        Assertions.assertTrue(productDao.reserveIfAvailable(product.getId()));
        Assertions.assertFalse(productDao.reserveIfAvailable(product.getId()));
        Assertions.assertTrue(productDao.findByIdIfAvailable(product.getId()).isEmpty());
        Assertions.assertTrue(productDao.findProducts(ProductSearchCriteria.empty()).getResults().isEmpty());
    }

    @Test
    public void markAsUserDeletedAndRestore() {
        final User user = userDao.createUser("seller5@test.com", "password", "seller5", false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
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
            BigDecimal.valueOf(1000)
        );

        Assertions.assertTrue(productDao.markAsUserDeleted(product.getId()));
        Assertions.assertFalse(productDao.markAsUserDeleted(product.getId()));
        Assertions.assertEquals(1, productDao.findProductsByUserIdAndState(user.getId(), ProductState.USER_DELETED, 1, 10).getResults().size());
        Assertions.assertTrue(productDao.findByIdIfAvailable(product.getId()).isEmpty());

        Assertions.assertTrue(productDao.restoreUserDeletedProduct(product.getId()));
        Assertions.assertFalse(productDao.restoreUserDeletedProduct(product.getId()));
        Assertions.assertTrue(productDao.findByIdIfAvailable(product.getId()).isPresent());
    }

    @Test
    public void markAsSoldOnlyFromReserved() {
        final User user = userDao.createUser("seller6@test.com", "password", "seller6", false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
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
            BigDecimal.valueOf(500)
        );

        Assertions.assertTrue(productDao.reserveIfAvailable(product.getId()));
        productDao.markAsSold(product.getId());
        Assertions.assertTrue(productDao.findByIdIfAvailable(product.getId()).isEmpty());
        Assertions.assertTrue(productDao.findById(product.getId()).isPresent());
    }

    @Test
    public void updateProductChangesTitleWhenActive() {
        final User user = userDao.createUser("seller7@test.com", "password", "seller7", false, true, null, null, null, null, null, null, null, null);
        final Product product = productDao.createProduct(
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
            BigDecimal.valueOf(2000)
        );

        Assertions.assertTrue(productDao.updateProduct(
            product.getId(),
            "NewTitle",
            "Artist",
            "Label",
            "CAT",
            "Argentina",
            Collections.emptyList(),
            "Desc",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(2500)
        ));

        final Product reloaded = productDao.findById(product.getId()).orElseThrow();
        Assertions.assertEquals("NewTitle", reloaded.getTitle());
        Assertions.assertEquals(0, reloaded.getPrice().compareTo(BigDecimal.valueOf(2500)));
    }
}
