package ar.edu.itba.paw.webapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ValidPaymentProofValidator implements ConstraintValidator<ValidPaymentProof, MultipartFile> {

    @Override
    public void initialize(ValidPaymentProof constraintAnnotation) {
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }
        try {
            PaymentProofValidator.validate(file);
            return true;
        } catch (PaymentProofValidator.InvalidPaymentProofException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessageKey()).addConstraintViolation();
            return false;
        }
    }
}
