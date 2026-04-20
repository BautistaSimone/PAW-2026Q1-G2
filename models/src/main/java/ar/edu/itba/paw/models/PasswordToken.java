package ar.edu.itba.paw.models;

import java.util.Date;

public class PasswordToken {
 
    private static final int EXPIRATION = 60 * 24;
 
    private Long tokenId;
    private String token;
    private Long userId;
    private Date expirationDate;

    public PasswordToken(final Long tokenId, final Long userId, final String token, final Date expirationDate) {
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

    public Date getExpirationDate() {
        return expirationDate;
    }
}