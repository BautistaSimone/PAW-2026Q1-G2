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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stateless signed token so a link in the report email can hide a product without admin login.
 */
@Component
public class ProductReportRemovalTokenService {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final Duration TOKEN_TTL = Duration.ofDays(14);
    private static final char SEP = ':';

    private final byte[] signingKey;

    @Autowired
    public ProductReportRemovalTokenService(
        @Value("${app.report.moderation.secret:}") final String explicitSecret,
        @Value("${auth.rememberme}") final String rememberMeFallback
    ) {
        final String keySource = (explicitSecret != null && !explicitSecret.isBlank())
            ? explicitSecret
            : rememberMeFallback;
        this.signingKey = keySource.getBytes(StandardCharsets.UTF_8);
    }

    public String createToken(final long productId) {
        final long expiryEpochSeconds = Instant.now().plus(TOKEN_TTL).getEpochSecond();
        final String payload = productId + String.valueOf(SEP) + expiryEpochSeconds;
        final String sig = sign(payload);
        final String combined = payload + String.valueOf(SEP) + sig;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @return true if token matches {@code productId}, signature is valid and token is not expired.
     */
    public boolean isValid(final long productId, final String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        final String combined;
        try {
            combined = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return false;
        }
        final int lastSep = combined.lastIndexOf(SEP);
        if (lastSep <= 0) {
            return false;
        }
        final String payload = combined.substring(0, lastSep);
        final String sig = combined.substring(lastSep + 1);
        if (!MessageDigest.isEqual(
            sign(payload).getBytes(StandardCharsets.UTF_8),
            sig.getBytes(StandardCharsets.UTF_8)
        )) {
            return false;
        }
        final String[] parts = payload.split(String.valueOf(SEP), 3);
        if (parts.length != 2) {
            return false;
        }
        try {
            final long id = Long.parseLong(parts[0]);
            final long exp = Long.parseLong(parts[1]);
            if (id != productId) {
                return false;
            }
            return Instant.now().getEpochSecond() <= exp;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String sign(final String payload) {
        try {
            final Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(signingKey, HMAC_ALG));
            final byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Cannot sign report removal token", e);
        }
    }
}
