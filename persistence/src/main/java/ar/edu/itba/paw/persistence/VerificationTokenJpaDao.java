package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import javax.sql.DataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.time.Instant;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.VerificationToken;

@Repository
public class VerificationTokenJpaDao implements VerificationTokenDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public VerificationToken createToken(final Long userId, final String token, final Instant expirationDate) {

        final VerificationToken verificationToken new VerificationToken(
            userId,
            token,
            expirationDate
        );

        em.persist(verificationToken);

        // Delete previous token, we don't want more than one per user
        em.createQuery("DELETE VerificationToken WHERE userId = :user_id")
            .setParameter("user_id", userId)
            .executeUpdate();

        return verificationToken;
    }

    @Override
    public Optional<VerificationToken> findByUserId(final Long userId) {
        final TypedQuery<User> query = em.createQuery("FROM VerificationToken WHERE userId = :user_id", VerificationToken.class);

        query.setParameter("user_id", userId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<VerificationToken> findByToken(final String token) {

        final TypedQuery<User> query = em.createQuery("FROM VerificationToken WHERE token = :token", VerificationToken.class);

        query.setParameter("token", token);
        return query.getResultList().stream().findFirst();
    }

}
