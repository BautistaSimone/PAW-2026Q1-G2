package ar.edu.itba.paw.models;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken extends Token {

    VerificationToken() {
        // Just for Hibernate, we love you!
    }

    public VerificationToken(final Long userId, final String token, final Instant expirationDate) {
        super(userId, token, expirationDate);
    }

}