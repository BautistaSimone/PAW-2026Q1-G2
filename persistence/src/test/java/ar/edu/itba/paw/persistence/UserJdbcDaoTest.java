package ar.edu.itba.paw.persistence;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.itba.paw.models.User;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class UserJdbcDaoTest {
    private UserJdbcDao userDao;

    @Test
    public void testCreateUserWhenUserDoesNotExist() {
        // 1. Arrange
        final String email = "[EMAIL_ADDRESS]";
        final String password = "[PASSWORD]";
        final String username = "[USERNAME]";

        // 2. Exercise
        final User user = userDao.createUser(email, password, username);

        // 3. Assert
        Assertions.assertNotNull(user);
        Assertions.assertEquals(username, user.getUsername());
        Assertions.assertEquals(password, user.getPassword());
    }
}