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

import ar.edu.itba.paw.models.PasswordToken;

@Repository
public class PasswordTokenJpaDao implements PasswordTokenDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public PasswordToken createToken(final Long userId, final String token, final Instant expirationDate) {

        final PasswordToken passwordToken = new PasswordToken(
            userId,
            token,
            expirationDate
        );

        // Delete previous token, we don't want more than one per user
        em.createQuery("DELETE FROM PasswordToken WHERE userId = :user_id")
            .setParameter("user_id", userId)
            .executeUpdate();

        em.persist(passwordToken);

        return passwordToken;
    }

    @Override
    public Optional<PasswordToken> findByUserId(final Long userId) {
        final TypedQuery<PasswordToken> query = em.createQuery("FROM PasswordToken WHERE userId = :user_id", PasswordToken.class);

        query.setParameter("user_id", userId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<PasswordToken> findByToken(final String token) {

        final TypedQuery<PasswordToken> query = em.createQuery("FROM PasswordToken WHERE token = :token", PasswordToken.class);

        query.setParameter("token", token);
        return query.getResultList().stream().findFirst();
    }

}
