package ar.edu.itba.paw.persistence;

import java.util.Date;
import java.util.Calendar;
import java.util.Optional;
import java.sql.Timestamp;
import java.util.UUID;
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

import ar.edu.itba.paw.models.Token;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class PasswordTokenJdbcDaoTest {

    private static final int EXPIRATION = 60 * 24;
 
    @Autowired
    private PasswordTokenJdbcDao passwordTokenDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private long userId;
    private String tkn;
    private Date expirationDate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);

        userId = jdbcTemplate.queryForObject(
            "INSERT INTO users (email, password, username, mod) VALUES ('user@test.com', 'pass', 'User', false) CALL IDENTITY()",
            Long.class
        );

        tkn = UUID.randomUUID().toString();
        expirationDate = calculateExpiryDate(EXPIRATION);

    }

    @Test
    public void testCreatePasswordToken() {
        final Token token = passwordTokenDao.createToken(userId, tkn, expirationDate);

        Assertions.assertNotNull(token);
        Assertions.assertEquals(expirationDate, token.getExpirationDate());
        Assertions.assertEquals(tkn, token.getToken());
        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "password_tokens"));
    }

    @Test
    public void testFindByUserId() {
        passwordTokenDao.createToken(userId, tkn, expirationDate);

        Optional<Token> result = passwordTokenDao.findByUserId(userId);

        Assertions.assertTrue(result.get().isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(tkn, result.get().getToken());
    }

    @Test
    public void testFindByToken() {
        passwordTokenDao.createToken(userId, tkn, expirationDate);

        Optional<Token> result = passwordTokenDao.findByToken(tkn);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(userId, result.get().getUserId());
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
