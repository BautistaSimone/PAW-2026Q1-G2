package ar.edu.itba.paw.persistence;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.sql.Timestamp;
import java.util.UUID;
import javax.sql.DataSource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

import ar.edu.itba.paw.models.VerificationToken;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class VerificationTokenJpaDaoTest {

    private static final int EXPIRATION = 60 * 24;
 
    @Autowired
    private VerificationTokenJpaDao verificationTokenDao;

    @PersistenceContext
    private EntityManager em;

    private long userId;
    private String tkn;
    private Instant expirationDate;

    @BeforeEach
    public void setUp() {
        User user = new User(
            "user@test.com",
            "pass",
            "User",
            false,
            true,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        em.persist(user);
        
        em.flush();

        userId = user.getId();

        tkn = UUID.randomUUID().toString();
        expirationDate = Instant.now().plus(Duration.ofMinutes(EXPIRATION));

        // Truncate it so that the precision lost doesn't make the tests fail
        expirationDate = expirationDate.truncatedTo(java.time.temporal.ChronoUnit.MICROS);

    }

    @Test
    public void testCreateVerificationToken() {
        // Arrange

        // Act
        final VerificationToken token = verificationTokenDao.createToken(userId, tkn, expirationDate);

        // Assert
        Assertions.assertNotNull(token);
        Assertions.assertEquals(expirationDate, token.getExpirationDate());
        Assertions.assertEquals(tkn, token.getToken());

        Long count = em.createQuery(
            "SELECT COUNT(vt) FROM VerificationToken vt",
            Long.class
        ).getSingleResult();

        Assertions.assertEquals(1L, count);
    }

    @Test
    public void testFindByUserId() {
        // Arrange
        verificationTokenDao.createToken(userId, tkn, expirationDate);

        // Act
        Optional<VerificationToken> result = verificationTokenDao.findByUserId(userId);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(tkn, result.get().getToken());
    }

    @Test
    public void testFindByToken() {
        // Arrange
        verificationTokenDao.createToken(userId, tkn, expirationDate);

        // Act
        Optional<VerificationToken> result = verificationTokenDao.findByToken(tkn);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(userId, result.get().getUserId());
    }

}
