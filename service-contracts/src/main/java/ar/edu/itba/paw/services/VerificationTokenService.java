package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.VerificationToken;
import ar.edu.itba.paw.models.User;

public interface VerificationTokenService {
    
    void createVerificationTokenForUser(final Long userId);
    
    boolean isValidVerificationToken(String token);
    Optional<VerificationToken> findByUserId(final Long userId);
    Optional<VerificationToken> findByToken(final String token);

}
