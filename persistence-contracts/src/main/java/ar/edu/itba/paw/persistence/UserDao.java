package ar.edu.itba.paw.persistence;

import java.util.Optional;

import ar.edu.itba.paw.models.User;

public interface UserDao {
    User createUser(final String email, final String password, final String username, Boolean mod);

    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);
} 