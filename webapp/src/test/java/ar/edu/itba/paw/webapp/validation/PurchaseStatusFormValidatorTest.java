package ar.edu.itba.paw.webapp.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.webapp.form.PurchaseStatusForm;

class PurchaseStatusFormValidatorTest {

    private final PurchaseStatusFormValidator validator = new PurchaseStatusFormValidator();

    private ConstraintValidatorContext context;
    private ConstraintViolationBuilder builder;
    private NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintViolationBuilder.class);
        nodeBuilder = mock(NodeBuilderCustomizableContext.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    private static PurchaseStatusForm form(final PurchaseStatus status, final MockMultipartFile proofFile) {
        final PurchaseStatusForm form = new PurchaseStatusForm();
        form.setNewStatus(status);
        form.setProofFile(proofFile);
        return form;
    }

    @Test
    void testIsValidWhenFormIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testIsValidWhenStatusIsNotPaid() {
        assertTrue(validator.isValid(form(PurchaseStatus.DELIVERED, null), context));
    }

    @Test
    void testIsValidWhenStatusIsPaidAndProofExists() {
        final MockMultipartFile proofFile = new MockMultipartFile(
            "proofFile", "proof.pdf", "application/pdf", "proof data".getBytes());

        assertTrue(validator.isValid(form(PurchaseStatus.PAID, proofFile), context));
    }

    @Test
    void testIsInvalidWhenStatusIsPaidAndProofIsMissing() {
        assertFalse(validator.isValid(form(PurchaseStatus.PAID, null), context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("PurchaseForm.proof.required");
        verify(builder).addPropertyNode("proofFile");
        verify(nodeBuilder).addConstraintViolation();
    }

    @Test
    void testIsInvalidWhenStatusIsPaidAndProofIsEmpty() {
        final MockMultipartFile emptyFile = new MockMultipartFile("proofFile", new byte[0]);

        assertFalse(validator.isValid(form(PurchaseStatus.PAID, emptyFile), context));

        verify(context).buildConstraintViolationWithTemplate("PurchaseForm.proof.required");
        verify(builder).addPropertyNode("proofFile");
        verify(nodeBuilder).addConstraintViolation();
    }
}
