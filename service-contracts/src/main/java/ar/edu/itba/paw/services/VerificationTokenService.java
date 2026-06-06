package ar.edu.itba.paw.services;

import java.util.Optional;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.VerificationToken;

public interface VerificationTokenService {

    void createVerificationTokenForUser(final Long userId);

    boolean isValidVerificationToken(String token);

    Optional<User> verifyEmail(final String token);

    Optional<VerificationToken> findByUserId(final Long userId);

    Optional<VerificationToken> findByToken(final String token);

}
