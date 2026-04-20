package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import ar.edu.itba.paw.models.PasswordToken;

public interface PasswordTokenDao {
    PasswordToken createToken(final Long userId, final String token, final Date expirationDate);

    Optional<PasswordToken> findByUserId(final Long userId);
    Optional<PasswordToken> findByToken(final String token);
}
