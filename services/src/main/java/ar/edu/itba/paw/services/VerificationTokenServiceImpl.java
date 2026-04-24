package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ar.edu.itba.paw.models.Token;
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

    private boolean isTokenExpired(Token verificationToken) {
        return verificationToken.getExpirationDate().before(new Date());
    }

    @Override
    public boolean isValidVerificationToken(String token) {
        final Optional<Token> verificationTokenOpt = verificationTokenDao.findByToken(token);

        if (!verificationTokenOpt.isPresent())
            return false;

        final Token verificationToken = verificationTokenOpt.get();
        return !isTokenExpired(verificationToken);
    }

    @Override
    @Transactional
    public void createVerificationTokenForUser(final Long userId) {

        final String token = UUID.randomUUID().toString();

        // TODO: Sacar de algun lado, que no sea magic number
        Date expiryDate = calculateExpiryDate(EXPIRATION);

        verificationTokenDao.createToken(userId, token, expiryDate);

        final User user = userService.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        runAfterCommit(() ->
            emailService.sendVerificationEmail(
                user.getEmail(),
                token,
                user.getUsername()
            )
        );
    }

    @Override
    public Optional<Token> findByUserId(final Long userId) {
		return verificationTokenDao.findByUserId(userId);
    }

    @Override
    public Optional<Token> findByToken(final String token) {
        return verificationTokenDao.findByToken(token);
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

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}
