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

        if (form.getNewStatus() == PurchaseStatus.PAID) {
            if (form.getProofFile() == null || form.getProofFile().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("PurchaseForm.proof.required")
                       .addPropertyNode("proofFile")
                       .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
