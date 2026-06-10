package ar.edu.itba.paw.models;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tokens_token_id_seq")
    @SequenceGenerator(sequenceName = "tokens_token_id_seq", name = "tokens_token_id_seq", allocationSize = 1)
    @Column(name = "token_id")
    private Long tokenId;
 
    @Column(nullable = false, unique = true)
    private final String token;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "expiration_date", nullable = false)
    private final Instant expirationDate;

    Token() {
        this.token = null;
        this.expirationDate = null;
    }

    public Token(final long userId, final String token, final Instant expirationDate) {
        super();
        this.userId = userId;
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public Long getTokenId() {
        return tokenId;
    }
}