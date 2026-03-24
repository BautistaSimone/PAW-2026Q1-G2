package ar.edu.itba.paw.persistence;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.User;

@Repository
public class UserJdbcDao implements UserDao {
    @Override
    public User createUser(final String email, final String password, final String username) {
        return new User(email, password, username);
    }
}