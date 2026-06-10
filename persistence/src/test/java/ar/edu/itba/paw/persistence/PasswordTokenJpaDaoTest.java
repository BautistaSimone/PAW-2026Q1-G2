package ar.edu.itba.paw.persistence;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.PasswordToken;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class PasswordTokenJpaDaoTest {

    private static final int EXPIRATION = 60 * 24;

    @Autowired
    private PasswordTokenJpaDao passwordTokenDao;

    @PersistenceContext
    private EntityManager em;

    private long userId;
    private String tkn;
    private Instant expirationDate;

    private PasswordToken insertPasswordToken() {
        final PasswordToken token = new PasswordToken(userId, tkn, expirationDate);
        em.persist(token);
        em.flush();
        return token;
    }

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
                null);

        em.persist(user);

        em.flush();

        userId = user.getId();

        tkn = UUID.randomUUID().toString();
        expirationDate = Instant.now().plus(Duration.ofMinutes(EXPIRATION));

        // Truncate it so that the precision lost doesn't make the tests fail
        expirationDate = expirationDate.truncatedTo(java.time.temporal.ChronoUnit.MICROS);

    }

    @Test
    public void testCreatePasswordToken() {
        // Arrange

        // Act
        final PasswordToken token = passwordTokenDao.createToken(userId, tkn, expirationDate);

        // Assert
        Assertions.assertNotNull(token);
        Assertions.assertEquals(expirationDate, token.getExpirationDate());
        Assertions.assertEquals(tkn, token.getToken());

        em.flush();
        em.clear();

        Long count = em.createQuery(
                "SELECT COUNT(pt) FROM PasswordToken pt",
                Long.class).getSingleResult();

        Assertions.assertEquals(1L, count);
    }

    @Test
    public void testFindByUserId() {
        // Arrange
        insertPasswordToken();
        em.clear();

        // Act
        Optional<PasswordToken> result = passwordTokenDao.findByUserId(userId);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(tkn, result.get().getToken());
    }

    @Test
    public void testFindByToken() {
        // Arrange
        insertPasswordToken();
        em.clear();

        // Act
        Optional<PasswordToken> result = passwordTokenDao.findByToken(tkn);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expirationDate, result.get().getExpirationDate());
        Assertions.assertEquals(userId, result.get().getUserId());
    }

    @Test
    public void testDeleteByToken() {
        // Arrange
        insertPasswordToken();
        em.clear();

        // Act
        passwordTokenDao.deleteByToken(tkn);
        em.flush();
        em.clear();

        // Assert
        Long count = em.createQuery(
                "SELECT COUNT(pt) FROM PasswordToken pt",
                Long.class).getSingleResult();

        Assertions.assertEquals(0L, count);
    }

}
