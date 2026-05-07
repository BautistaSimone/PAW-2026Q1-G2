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
@Table(name = "password_tokens")
public class PasswordToken extends Token {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "password_tokens_token_id_seq")
    @SequenceGenerator(sequenceName = "password_tokens_token_id_seq", name = "password_tokens_token_id_seq", allocationSize = 1)
    @Column(name = "token_id")
    private Long tokenId;

    PasswordToken() {
        // Just for Hibernate, we love you!
        super();
    }

    public PasswordToken(final Long userId, final String token, final Instant expirationDate) {
        super(userId, token, expirationDate);
    }

    public Long getTokenId() {
        return tokenId;
    }
}