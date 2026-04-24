package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import ar.edu.itba.paw.models.Token;

public interface VerificationTokenDao {
    Token createToken(final Long userId, final String token, final Date expirationDate);

    Optional<Token> findByUserId(final Long userId);
    Optional<Token> findByToken(final String token);
}
