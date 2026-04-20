package ar.edu.itba.paw.models;

import java.time.LocalDate;

public class PasswordToken {
 
    private static final int EXPIRATION = 60 * 24;
 
    private Long tokenId;
    private String token;
    private Long userId;
    private LocalDate expirationDate;

    public PasswordToken(final Long tokenId, final Long userId, final String token, final LocalDate expirationDate) {
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

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}