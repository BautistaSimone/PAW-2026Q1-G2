package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import ar.edu.itba.paw.models.Token;

public interface PasswordTokenDao {
    Token createToken(final Long userId, final String token, final Instant expirationDate);

    Optional<Token> findByUserId(final Long userId);
    Optional<Token> findByToken(final String token);
}
