package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

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

@Rollback   // Clean database before testing
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class UserJdbcDaoTest {

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
    public void testCreateUserWhenUserDoesNotExist() {
        // 1. Arrange
        final String email = "[EMAIL_ADDRESS]";
        final String password = "[PASSWORD]";
        final String username = "[USERNAME]";
        final Boolean mod = false;

        // 2. Exercise
        final User user = userDao.createUser(email, password, username, mod);

        // 3. Assert
        Assertions.assertNotNull(user);
        Assertions.assertEquals(username, user.getUsername());
        Assertions.assertEquals(password, user.getPassword());
        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "users"));
    }
}