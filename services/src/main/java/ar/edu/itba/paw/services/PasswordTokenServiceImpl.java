package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ar.edu.itba.paw.models.PasswordToken;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.PasswordTokenDao;

@Service
public class PasswordTokenServiceImpl implements PasswordTokenService {

    private final PasswordTokenDao passwordTokenDao;

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public PasswordTokenServiceImpl(
        final PasswordTokenDao passwordTokenDao,
            final UserService userService,
            final EmailService emailService
            ) {
        this.passwordTokenDao = passwordTokenDao;
        this.userService = userService;
        this.emailService = emailService;
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
        return !isTokenExpired(passToken);
    }

    @Override
    @Transactional
    public void createPasswordResetTokenForUser(final Long userId, String token) {

        // TODO: Sacar de algun lado, que no sea magic number
        Date expiryDate = new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000); // Expires in one day

        passwordTokenDao.createToken(userId, token, expiryDate);

        final User user = userService.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        runAfterCommit(() ->
            emailService.sendPasswordResetEmail(
                user.getEmail(),
                token,
                user.getUsername()
            )
        );
    }

    @Override
    public Optional<PasswordToken> findByUserId(final Long userId) {
		return passwordTokenDao.findByUserId(userId);
    }

    @Override
    public Optional<PasswordToken> findByToken(final String token) {
        return passwordTokenDao.findByToken(token);
    }

    private static void runAfterCommit(final Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

}
