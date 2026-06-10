package ar.edu.itba.paw.webapp.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = EmailAvailableValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface EmailAvailable {

    String message() default "{EmailInUse.authForm.email}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}