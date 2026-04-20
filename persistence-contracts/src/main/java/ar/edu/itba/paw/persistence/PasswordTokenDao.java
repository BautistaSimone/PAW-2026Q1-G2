package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.PasswordToken;

public interface PasswordTokenDao {

    Optional<PasswordToken> findByUserId(final Long userId);
}
