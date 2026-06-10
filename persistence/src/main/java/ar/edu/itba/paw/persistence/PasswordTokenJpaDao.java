package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Optional;
import java.time.Instant;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.PasswordToken;

@Repository
public class PasswordTokenJpaDao implements PasswordTokenDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public PasswordToken createToken(final Long userId, final String token, final Instant expirationDate) {
        final PasswordToken passwordToken = new PasswordToken(userId, token, expirationDate);

        em.createQuery("DELETE FROM PasswordToken WHERE userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();

        em.persist(passwordToken);
        return passwordToken;
    }

    @Override
    public Optional<PasswordToken> findByUserId(final Long userId) {
        final TypedQuery<PasswordToken> query = em.createQuery(
            "FROM PasswordToken WHERE userId = :userId", PasswordToken.class
        );
        query.setParameter("userId", userId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<PasswordToken> findByToken(final String token) {
        final TypedQuery<PasswordToken> query = em.createQuery(
            "FROM PasswordToken WHERE token = :token", PasswordToken.class
        );
        query.setParameter("token", token);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public void deleteByToken(final String token) {
        em.createQuery("DELETE FROM PasswordToken WHERE token = :token")
            .setParameter("token", token)
            .executeUpdate();
    }
}
