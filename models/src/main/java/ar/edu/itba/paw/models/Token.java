package ar.edu.itba.paw.models;

import java.time.Instant;

abstract class Token {
 
    protected String token;
    protected Long userId;
    protected Instant expirationDate;

    public Token(final Long userId, final String token, final Instant expirationDate) {
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