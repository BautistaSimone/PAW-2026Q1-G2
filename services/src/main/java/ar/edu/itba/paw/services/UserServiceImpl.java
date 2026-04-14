package ar.edu.itba.paw.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.models.User;

@Service
public class UserServiceImpl implements UserService{

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

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

        LOGGER.atDebug().addArgument(encodedPassword).log("About to attempt register user {}");

		return userDao.createUser(email, encodedPassword, username, mod);
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
