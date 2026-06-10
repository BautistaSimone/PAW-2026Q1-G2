package ar.edu.itba.paw.models;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "password_tokens")
public class PasswordToken extends Token {

    PasswordToken() {
        // Just for Hibernate, we love you!
        super();
    }

    public PasswordToken(final long userId, final String token, final Instant expirationDate) {
        super(userId, token, expirationDate);
    }

}