package ar.edu.itba.paw.services;

import java.util.Optional;
import java.time.Instant;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.VerificationToken;
import ar.edu.itba.paw.persistence.VerificationTokenDao;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceImplTest {

    private static final int EXPIRATION = 60 * 24;
 
    @InjectMocks
    private VerificationTokenServiceImpl verificationTokenService;

    @Mock
    private VerificationTokenDao verificationTokenDao;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Test
    public void testAcceptVerificationTokenWhenNotExpired() {
        VerificationToken token = new VerificationToken(
            1L,
            "token",
            Instant.now().plus(Duration.ofMinutes(EXPIRATION))
        );

        Mockito.when(verificationTokenDao.findByToken("token")).thenReturn(Optional.of(token));

        boolean result = verificationTokenService.isValidVerificationToken("token");

        Assertions.assertTrue(result);
    }

    @Test
    public void testRejectVerificationTokenWhenExpired() {
        VerificationToken token = new VerificationToken(
            1L,
            "token",
            Instant.now().minus(Duration.ofMinutes(1))
        );

        Mockito.when(verificationTokenDao.findByToken("token")).thenReturn(Optional.of(token));

        boolean result = verificationTokenService.isValidVerificationToken("token");

        Assertions.assertFalse(result);
    }

    @Test
    public void testVerifyEmailEnablesTokenUser() {
        VerificationToken token = new VerificationToken(
            1L,
            "token",
            Instant.now().plus(Duration.ofMinutes(EXPIRATION))
        );
        User user = new User(
            1L,
            "user@test.com",
            "password",
            "user",
            false,
            true,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        Mockito.when(verificationTokenDao.findByToken("token")).thenReturn(Optional.of(token));
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = verificationTokenService.verifyEmail("token");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1L, result.get().getId());
        Mockito.verify(userService).enable(1L);
    }

    @Test
    public void testVerifyEmailDoesNotEnableExpiredToken() {
        VerificationToken token = new VerificationToken(
            1L,
            "token",
            Instant.now().minus(Duration.ofMinutes(1))
        );

        Mockito.when(verificationTokenDao.findByToken("token")).thenReturn(Optional.of(token));

        Optional<User> result = verificationTokenService.verifyEmail("token");

        Assertions.assertFalse(result.isPresent());
        Mockito.verify(userService, Mockito.never()).enable(Mockito.anyLong());
    }

}
