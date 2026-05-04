package ar.edu.itba.paw.services;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Stateless signed token so a link in the report email can hide a product without admin login.
 */
public interface ProductReportRemovalTokenService {

    String createToken(final long productId);

    /**
     * @return true if token matches {@code productId}, signature is valid and token is not expired.
     */
    boolean isValid(final long productId, final String token);
}
