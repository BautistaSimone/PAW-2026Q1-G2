package ar.edu.itba.paw.models;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken extends Token {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verification_tokens_token_id_seq")
    @SequenceGenerator(sequenceName = "verification_tokens_token_id_seq", name = "verification_tokens_token_id_seq", allocationSize = 1)
    @Column(name = "token_id")
    private Long tokenId;

    VerificationToken() {
        // Just for Hibernate, we love you!
    }

    public VerificationToken(final Long userId, final String token, final Instant expirationDate) {
        super(userId, token, expirationDate);
    }

    public VerificationToken(final Long tokenId, final Long userId, final String token, final Instant expirationDate) {
        super(userId, token, expirationDate);

        this.tokenId = tokenId;
    }

    public Long getTokenId() {
        return tokenId;
    }
}