package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

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
public class UserJpaDaoTest {

    @Autowired
    private UserJpaDao userDao;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testCreateUserWhenUserDoesNotExist() {
        // 1. Arrange
        final String email = "[EMAIL_ADDRESS]";
        final String password = "[PASSWORD]";
        final String username = "[USERNAME]";
        final Boolean mod = false;
        final Boolean enabled = false;

        // 2. Exercise
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

        // 3. Assert
        Assertions.assertNotNull(user);
        Assertions.assertEquals(username, user.getUsername());
        Assertions.assertEquals(password, user.getPassword());

        Long count = em.createQuery(
            "SELECT COUNT(u) FROM User u",
            Long.class
        ).getSingleResult();

        Assertions.assertEquals(1L, count);
    }
}