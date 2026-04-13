package ar.edu.itba.paw.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.models.User;

@Service
public class UserServiceImpl implements UserService{
	private UserDao userDao;
    private final PasswordEncoder passwordEncoder;

	@Autowired
	public UserServiceImpl(final UserDao userDao, final PasswordEncoder passwordEncoder) {
		this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
	}

	@Override
	public User createUser(final String email, final String password, final String username, Boolean mod) {

		// Encode password before storing
		final String encodedPassword = passwordEncoder.encode(password);
		return userDao.createUser(email, password, username, mod);
	}

	@Override
	public Optional<User> findByEmail(final String email) {
		return userDao.findByEmail(email);
	}

	@Override
	public Optional<User> findById(final Long id) {
		return userDao.findById(id);
	}
}
