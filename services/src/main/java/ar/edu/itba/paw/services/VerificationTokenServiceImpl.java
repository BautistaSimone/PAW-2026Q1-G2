package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.time.Duration;
import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ar.edu.itba.paw.models.VerificationToken;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.VerificationTokenDao;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final int EXPIRATION = 60 * 24;
 
    private final VerificationTokenDao verificationTokenDao;

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public VerificationTokenServiceImpl(
        final VerificationTokenDao verificationTokenDao,
            final UserService userService,
            final EmailService emailService
            ) {
        this.verificationTokenDao = verificationTokenDao;
        this.userService = userService;
        this.emailService = emailService;
    }

    private boolean isTokenExpired(VerificationToken verificationToken) {
        return verificationToken.getExpirationDate().isBefore(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidVerificationToken(String token) {
        final Optional<VerificationToken> verificationTokenOpt = verificationTokenDao.findByToken(token);

        if (!verificationTokenOpt.isPresent())
            return false;

        final VerificationToken verificationToken = verificationTokenOpt.get();
        return !isTokenExpired(verificationToken);
    }

    @Override
    @Transactional
    public void createVerificationTokenForUser(final Long userId) {

        final String token = UUID.randomUUID().toString();

        Instant expiryDate = Instant.now().plus(Duration.ofMinutes(EXPIRATION));

        verificationTokenDao.createToken(userId, token, expiryDate);

        final User user = userService.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        runAfterCommit(() ->
            emailService.sendVerificationEmail(
                user.getEmail(),
                token,
                user.getUsername(),
                LocaleContextHolder.getLocale()
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerificationToken> findByUserId(final Long userId) {
		return verificationTokenDao.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerificationToken> findByToken(final String token) {
        return verificationTokenDao.findByToken(token);
    }

    private static void runAfterCommit(final Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                @Transactional
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
