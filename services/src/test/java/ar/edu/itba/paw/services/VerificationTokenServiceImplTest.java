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

import ar.edu.itba.paw.models.Token;
import ar.edu.itba.paw.persistence.VerificationTokenDao;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceImplTest {

    private static final int EXPIRATION = 60 * 24;
 
    @InjectMocks
    private VerificationTokenServiceImpl verificationTokenService;

    @Mock
    private VerificationTokenDao verificationTokenDao;

    @Test
    public void testAcceptVerificationTokenWhenNotExpired() {
        Token token = new Token(
            1L,
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
        Token token = new Token(
            1L,
            1L,
            "token",
            Instant.now()
        );

        Mockito.when(verificationTokenDao.findByToken("token")).thenReturn(Optional.of(token));

        boolean result = verificationTokenService.isValidVerificationToken("token");

        Assertions.assertFalse(result);
    }

}
