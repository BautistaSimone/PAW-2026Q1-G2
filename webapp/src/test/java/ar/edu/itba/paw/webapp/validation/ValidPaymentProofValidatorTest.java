package ar.edu.itba.paw.webapp.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ValidPaymentProofValidatorTest {

    private final ValidPaymentProofValidator validator = new ValidPaymentProofValidator();

    private ConstraintValidatorContext context;
    private ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    @Test
    void testIsValidWithNullFile() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testIsValidWithEmptyFile() {
        final MockMultipartFile emptyFile = new MockMultipartFile("proofFile", new byte[0]);

        assertTrue(validator.isValid(emptyFile, context));
    }

    @Test
    void testIsValidWithValidPdf() {
        final MockMultipartFile pdfFile = new MockMultipartFile(
            "proofFile",
            "proof.pdf",
            "application/pdf",
            "%PDF-1.4\nsome pdf content\n%%EOF".getBytes(StandardCharsets.US_ASCII));

        assertTrue(validator.isValid(pdfFile, context));
    }

    @Test
    void testIsInvalidWithWrongMimeType() {
        final MockMultipartFile textFile = new MockMultipartFile(
            "proofFile",
            "proof.txt",
            "text/plain",
            "this is neither a pdf nor an image".getBytes(StandardCharsets.UTF_8));

        assertFalse(validator.isValid(textFile, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("PurchaseForm.proof.invalid");
        verify(builder).addConstraintViolation();
    }
}
