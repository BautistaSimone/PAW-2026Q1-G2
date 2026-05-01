package ar.edu.itba.paw.persistence;

import java.time.Instant;
import java.time.Duration;
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
public class VerificationTokenJdbcDaoTest {

    private static final int EXPIRATION = 60 * 24;
 
    @Autowired
    private VerificationTokenJdbcDao verificationTokenDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private long userId;
    private String tkn;
    private Instant expirationDate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);

        userId = jdbcTemplate.queryForObject(
            "INSERT INTO users (email, password, username, mod) VALUES ('user@test.com', 'pass', 'User', false) CALL IDENTITY()",
            Long.class
        );

        tkn = UUID.randomUUID().toString();
        expirationDate = Instant.now().plus(Duration.ofMinutes(EXPIRATION));

        // Truncate it so that the precision lost doesn't make the tests fail
        expirationDate = expirationDate.truncatedTo(java.time.temporal.ChronoUnit.MICROS);

    }

    @Test
    public void testCreateVerificationToken() {
        final Token token = verificationTokenDao.createToken(userId, tkn, expirationDate);

        Assertions.assertNotNull(token);
        Assertions.assertEquals(expirationDate, token.getExpirationDate());
        Assertions.assertEquals(tkn, token.getToken());
        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "verification_tokens"));
    }

    @Test
    public void testFindByUserId() {
        verificationTokenDao.createToken(userId, tkn, expirationDate);

        Optional<Token> result = verificationTokenDao.findByUserId(userId);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(tkn, result.get().getToken());
    }

    @Test
    public void testFindByToken() {
        verificationTokenDao.createToken(userId, tkn, expirationDate);

        Optional<Token> result = verificationTokenDao.findByToken(tkn);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(userId, result.get().getUserId());
    }

}
