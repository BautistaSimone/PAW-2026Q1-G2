package ar.edu.itba.paw.webapp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldMatchValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();
    }

    @Test
    void testIsValidWhenFieldsMatch() {
        final Set<ConstraintViolation<PasswordForm>> violations =
            validator.validate(new PasswordForm("secret", "secret"));

        assertTrue(violations.isEmpty());
    }

    @Test
    void testIsValidWhenBothFieldsAreNull() {
        assertTrue(validator.validate(new PasswordForm(null, null)).isEmpty());
    }

    @Test
    void testIsInvalidWhenFieldsDoNotMatch() {
        final Set<ConstraintViolation<PasswordForm>> violations =
            validator.validate(new PasswordForm("secret", "different"));

        assertEquals(1, violations.size());
        final ConstraintViolation<PasswordForm> violation = violations.iterator().next();
        assertEquals("repeatPassword", violation.getPropertyPath().toString());
    }

    @FieldMatch(first = "password", second = "repeatPassword")
    private static final class PasswordForm {
        private final String password;
        private final String repeatPassword;

        private PasswordForm(final String password, final String repeatPassword) {
            this.password = password;
            this.repeatPassword = repeatPassword;
        }
    }
}
