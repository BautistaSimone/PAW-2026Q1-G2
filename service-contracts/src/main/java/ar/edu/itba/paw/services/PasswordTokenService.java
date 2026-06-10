package ar.edu.itba.paw.services;

import java.util.Optional;

import ar.edu.itba.paw.models.PasswordToken;

public interface PasswordTokenService {
    void createPasswordResetTokenForUser(final Long userId, String token);

    boolean isValidPasswordResetToken(String token);

    Optional<PasswordToken> findByUserId(final Long userId);

}
