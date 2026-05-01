package ar.edu.itba.paw.models;

import java.time.Instant;

public class Token {
 
    private Long tokenId;
    private String token;
    private Long userId;
    private Instant expirationDate;

    public Token(final Long tokenId, final Long userId, final String token, final Instant expirationDate) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public Long getTokenId() {
        return tokenId;
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