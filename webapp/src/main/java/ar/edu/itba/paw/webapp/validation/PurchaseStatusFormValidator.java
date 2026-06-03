package ar.edu.itba.paw.webapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.webapp.form.PurchaseStatusForm;

public class PurchaseStatusFormValidator implements ConstraintValidator<ValidPurchaseStatusForm, PurchaseStatusForm> {

    @Override
    public void initialize(final ValidPurchaseStatusForm constraintAnnotation) {
    }

    @Override
    public boolean isValid(final PurchaseStatusForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        PurchaseStatus statusObj = null;
        try {
            statusObj = PurchaseStatus.valueOf(form.getNewStatus());
        } catch (IllegalArgumentException | NullPointerException e) {
            // Let standard validation or controller handle invalid status values
            return true;
        }

        if (statusObj == PurchaseStatus.PAID) {
            try {
                PaymentProofValidator.validate(form.getProofFile());
            } catch (PaymentProofValidator.InvalidPaymentProofException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(e.getMessageKey())
                       .addPropertyNode("proofFile")
                       .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
