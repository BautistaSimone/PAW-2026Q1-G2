package ar.edu.itba.paw.persistence;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class UserJdbcDaoTest {
    private UserJdbcDao userDao;

}