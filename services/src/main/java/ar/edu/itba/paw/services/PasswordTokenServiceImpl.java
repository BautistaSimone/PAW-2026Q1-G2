package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;
import java.util.Date;

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

    private boolean isTokenExpired(PasswordToken passToken) {
        return passToken.getExpirationDate().before(new Date());
    }

    @Override
    public boolean isValidPasswordResetToken(String token) {
        final Optional<PasswordToken> passTokenOpt = passwordTokenDao.findByToken(token);

        if (!passTokenOpt.isPresent())
            return false;

        final PasswordToken passToken = passTokenOpt.get();
        return isTokenExpired(passToken);
    }

    @Override
    public void createPasswordResetTokenForUser(final Long userId, String token) {

        // TODO: Sacar de algun lado, que no sea magic number
        Date expiryDate = new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000); // Expires in one day

        passwordTokenDao.createToken(userId, token, expiryDate);
    }

    @Override
    public Optional<PasswordToken> findByUserId(final Long userId) {
		return passwordTokenDao.findByUserId(userId);
    }
}
