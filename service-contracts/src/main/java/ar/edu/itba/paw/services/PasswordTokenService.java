package ar.edu.itba.paw.services;

import java.util.Optional;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.PasswordToken;

public interface PasswordTokenService {
    void createPasswordResetTokenForUser(final User user);

    boolean isValidPasswordResetToken(String token);

    Optional<PasswordToken> findByUserId(final Long userId);

    Optional<PasswordToken> findByToken(String token);

    void deleteByToken(String token);

}
