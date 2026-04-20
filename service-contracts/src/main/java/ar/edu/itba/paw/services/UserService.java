package ar.edu.itba.paw.services;

import java.util.Optional;

import ar.edu.itba.paw.models.User;

public interface UserService {
    User createUser(final String email, final String password, final String username, Boolean mod);
	
    void updatePassword(final Long userId, final String newPassword);

	Optional<User> findByEmail(final String email);
	Optional<User> findById(final Long id);

}
