package ar.edu.itba.paw.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.models.User;

@Service
public class UserServiceImpl implements UserService{
	private UserDao userDao;

	@Autowired
	public UserServiceImpl(final UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public User createUser(final String email, final String password, final String username, Boolean mod) {
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
