package ar.edu.itba.paw.webapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.edu.itba.paw.services.UserService;

@Component
public class EmailAvailableValidator implements ConstraintValidator<EmailAvailable, String> {

    @Autowired
    private UserService userService;

    @Override
    public void initialize(final EmailAvailable constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(final String email, final ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true; // @NotBlank handles this
        }
        return !userService.findByEmail(email).isPresent();
    }
}