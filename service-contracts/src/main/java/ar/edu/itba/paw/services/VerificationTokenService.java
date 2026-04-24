package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Token;
import ar.edu.itba.paw.models.User;

public interface VerificationTokenService {
    
    void createVerificationTokenForUser(final Long userId);
    
    boolean isValidVerificationToken(String token);
    Optional<Token> findByUserId(final Long userId);
    Optional<Token> findByToken(final String token);

}
