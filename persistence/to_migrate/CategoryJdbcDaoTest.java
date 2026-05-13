package ar.edu.itba.paw.persistence;

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

import ar.edu.itba.paw.models.Category;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class CategoryJdbcDaoTest {

    @Autowired
    private CategoryJdbcDao categoryDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private long insertCategory(final String name) {
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject("CALL IDENTITY()", Long.class);
    }

    private long insertUser(final String suffix) {
        jdbcTemplate.update(
            "INSERT INTO users (email, password, username, mod) VALUES (?, 'pass', ?, false)",
            suffix + "@test.com",
            suffix
        );
        return jdbcTemplate.queryForObject("CALL IDENTITY()", Long.class);
    }

    private long insertProduct(final long userId, final String title) {
        jdbcTemplate.update(
            "INSERT INTO products (user_id, title, artist, description, sleeve_condition, record_condition, published, price) "
                + "VALUES (?, ?, 'Artist', 'Description', 8, 9, CURRENT_DATE, 1000)",
            userId,
            title
        );
        return jdbcTemplate.queryForObject("CALL IDENTITY()", Long.class);
    }

    @Test
    public void findAllReturnsEveryCategorySortedByName() {
        insertCategory("Rock");
        insertCategory("Ambient");
        insertCategory("Jazz");

        final List<Category> categories = categoryDao.findAll();

        Assertions.assertEquals(3, JdbcTestUtils.countRowsInTable(jdbcTemplate, "categories"));
        Assertions.assertEquals(List.of("Ambient", "Jazz", "Rock"), categories.stream().map(Category::getName).toList());
    }

    @Test
    public void findByProductIdReturnsOnlyLinkedCategoriesSortedByName() {
        final long userId = insertUser("category-seller");
        final long productId = insertProduct(userId, "Album");
        final long otherProductId = insertProduct(userId, "Other album");
        final long rockId = insertCategory("Rock");
        final long ambientId = insertCategory("Ambient");
        final long jazzId = insertCategory("Jazz");
        jdbcTemplate.update("INSERT INTO products_categories (product_id, category_id) VALUES (?, ?)", productId, rockId);
        jdbcTemplate.update("INSERT INTO products_categories (product_id, category_id) VALUES (?, ?)", productId, ambientId);
        jdbcTemplate.update("INSERT INTO products_categories (product_id, category_id) VALUES (?, ?)", otherProductId, jazzId);

        final List<Category> categories = categoryDao.findByProductId(productId);

        Assertions.assertEquals(List.of("Ambient", "Rock"), categories.stream().map(Category::getName).toList());
    }

    @Test
    public void findByProductIdReturnsEmptyWhenProductHasNoCategories() {
        final long userId = insertUser("category-empty-seller");
        final long productId = insertProduct(userId, "Album without categories");
        insertCategory("Unlinked");

        final List<Category> categories = categoryDao.findByProductId(productId);

        Assertions.assertTrue(categories.isEmpty());
    }
}
