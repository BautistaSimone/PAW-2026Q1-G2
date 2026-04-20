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
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    private static String trimToNull(final String s) {
        if (s == null) {
            return null;
        }
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public User createUser(
            final String email,
            final String password,
            final String username,
            final Boolean mod,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu) {

        final String encodedPassword = passwordEncoder.encode(password);

        LOGGER.atDebug().addArgument(email).log("About to attempt register user {}");

        return userDao.createUser(
                email,
                encodedPassword,
                username,
                mod,
                trimToNull(firstName),
                trimToNull(lastName),
                trimToNull(streetName),
                trimToNull(streetNumber),
                trimToNull(neighborhood),
                trimToNull(province),
                trimToNull(extraAddressInfo),
                trimToNull(cbuCvu));
    }

    @Override
    public void updateUserProfile(
            final Long userId,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu) {
        userDao.updateUserProfile(
                userId,
                trimToNull(firstName),
                trimToNull(lastName),
                trimToNull(streetName),
                trimToNull(streetNumber),
                trimToNull(neighborhood),
                trimToNull(province),
                trimToNull(extraAddressInfo),
                trimToNull(cbuCvu));
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }
    @Override
	public void updatePassword(final Long userId, final String newPassword) {
		// Encode password before storing
		final String encodedPassword = passwordEncoder.encode(newPassword);

		userDao.updatePassword(userId, encodedPassword);
	}

    @Override
    public Optional<User> findById(final Long id) {
        return userDao.findById(id);
    }
}
