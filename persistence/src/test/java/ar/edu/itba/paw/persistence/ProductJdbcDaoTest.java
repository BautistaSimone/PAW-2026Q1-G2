package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.Collections;

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
        final User user = userDao.createUser("seller@test.com", "password", "seller", false);

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
            "Palermo",
            "CABA",
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
            "Recoleta",
            "CABA",
            BigDecimal.valueOf(28000)
        );

        Assertions.assertNotNull(firstProduct);
        Assertions.assertNotNull(secondProduct);
        Assertions.assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "products"));
        Assertions.assertEquals(2, productDao.listProducts().size());
    }
}
