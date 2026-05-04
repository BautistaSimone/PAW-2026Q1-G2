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
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class CategoryJdbcDaoTest {

    @Autowired
    private CategoryJdbcDao categoryDao;

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

    private Long createCategory(final String name) {
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject(
            "SELECT category_id FROM categories WHERE name = ?",
            Long.class,
            name
        );
    }

    private User createUser(final String email) {
        return userDao.createUser(email, "password", email, false, true, null, null, null, null, null, null, null, null);
    }

    @Test
    public void findAllReturnsCategoriesSortedByName() {
        createCategory("Rock");
        createCategory("Ambient");
        createCategory("Jazz");

        final List<Category> categories = categoryDao.findAll();

        Assertions.assertEquals(3, categories.size());
        Assertions.assertIterableEquals(
            List.of("Ambient", "Jazz", "Rock"),
            categories.stream().map(Category::getName).toList()
        );
    }

    @Test
    public void findByProductIdReturnsOnlyProductCategoriesSortedByName() {
        final Long soulId = createCategory("Soul");
        final Long bluesId = createCategory("Blues");
        createCategory("Punk");
        final User seller = createUser("category-seller@test.com");
        final Product product = productDao.createProduct(
            seller.getId(),
            "Kind of Blue",
            "Miles Davis",
            "Columbia",
            "CL-1355",
            "USA",
            List.of(soulId, bluesId),
            "Original pressing",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(1000)
        );

        final List<Category> categories = categoryDao.findByProductId(product.getId());

        Assertions.assertEquals(2, categories.size());
        Assertions.assertIterableEquals(
            List.of("Blues", "Soul"),
            categories.stream().map(Category::getName).toList()
        );
    }

    @Test
    public void findByProductIdReturnsEmptyListWhenProductHasNoCategories() {
        final User seller = createUser("category-empty-seller@test.com");
        final Product product = productDao.createProduct(
            seller.getId(),
            "No Genre",
            "Unknown Artist",
            "Indie Label",
            "NG-001",
            "Argentina",
            Collections.emptyList(),
            "No category assigned",
            BigDecimal.valueOf(8.0),
            BigDecimal.valueOf(8.0),
            BigDecimal.valueOf(500)
        );

        Assertions.assertTrue(categoryDao.findByProductId(product.getId()).isEmpty());
    }
}
