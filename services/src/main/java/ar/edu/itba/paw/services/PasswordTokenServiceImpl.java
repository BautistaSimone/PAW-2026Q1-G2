package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.PasswordToken;
import ar.edu.itba.paw.persistence.PasswordTokenDao;

@Service
public class PasswordTokenServiceImpl implements PasswordTokenService {

    private final PasswordTokenDao passwordTokenDao;

    @Autowired
    public PasswordTokenServiceImpl(final PasswordTokenDao passwordTokenDao) {
        this.passwordTokenDao = passwordTokenDao;
    }

    @Override
    public void createPasswordResetTokenForUser(final Long userId, String token) {

    }
    
    @Override
    public Optional<PasswordToken> findByUserId(final Long userId) {
		return passwordTokenDao.findByUserId(userId);
    }
}
