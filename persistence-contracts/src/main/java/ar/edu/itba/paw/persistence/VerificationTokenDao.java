package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import ar.edu.itba.paw.models.VerificationToken;

public interface VerificationTokenDao {
    VerificationToken createToken(final Long userId, final String token, final Instant expirationDate);

    Optional<VerificationToken> findByUserId(final Long userId);
    Optional<VerificationToken> findByToken(final String token);
}
