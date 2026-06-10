package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Optional;
import java.time.Instant;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.VerificationToken;

@Repository
public class VerificationTokenJpaDao implements VerificationTokenDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public VerificationToken createToken(final Long userId, final String token, final Instant expirationDate) {
        final VerificationToken verificationToken = new VerificationToken(userId, token, expirationDate);

        em.createQuery("DELETE FROM VerificationToken WHERE userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();

        em.persist(verificationToken);
        return verificationToken;
    }

    @Override
    public Optional<VerificationToken> findByUserId(final Long userId) {
        final TypedQuery<VerificationToken> query = em.createQuery(
            "FROM VerificationToken WHERE userId = :userId", VerificationToken.class
        );
        query.setParameter("userId", userId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<VerificationToken> findByToken(final String token) {
        final TypedQuery<VerificationToken> query = em.createQuery(
            "FROM VerificationToken WHERE token = :token", VerificationToken.class
        );
        query.setParameter("token", token);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public void deleteByToken(final String token) {
        em.createQuery("DELETE FROM VerificationToken WHERE token = :token")
            .setParameter("token", token)
            .executeUpdate();
    }
}
