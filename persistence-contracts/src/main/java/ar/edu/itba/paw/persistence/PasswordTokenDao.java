package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import ar.edu.itba.paw.models.PasswordToken;

public interface PasswordTokenDao {
    Token createToken(final Long userId, final String token, final Instant expirationDate);

    Optional<PasswordToken> findByUserId(final Long userId);
    Optional<PasswordToken> findByToken(final String token);
}
