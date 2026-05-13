package ar.edu.itba.paw.models;

import java.time.Instant;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
abstract class Token {
 
    protected String token;
    protected Long userId;
    protected Instant expirationDate;

    Token() {

    }

    public Token(final Long userId, final String token, final Instant expirationDate) {
        super();
        this.userId = userId;
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

}