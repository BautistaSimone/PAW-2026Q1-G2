package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.PasswordToken;
import ar.edu.itba.paw.models.User;

public interface PasswordTokenService {
    void createPasswordResetTokenForUser(final Long userId, String token);
    boolean isValidPasswordResetToken(String token);
    Optional<PasswordToken> findByUserId(final Long userId);
    Optional<PasswordToken> findByToken(final String token);
}
