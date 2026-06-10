package ar.edu.itba.paw.webapp.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ValidImageValidatorTest {

    private final ValidImageValidator validator = new ValidImageValidator();

    private ConstraintValidatorContext context;
    private ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    }

    @Test
    void testIsValidWithNullArray() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testIsValidWithEmptyArray() {
        assertTrue(validator.isValid(new MultipartFile[0], context));
    }

    @Test
    void testIsValidWithValidImages() throws Exception {
        final MultipartFile[] files = new MultipartFile[] {
            new MockMultipartFile("images", "cover.png", "image/png", validPng()),
            new MockMultipartFile("images", "back.png", "image/png", validPng())
        };

        assertTrue(validator.isValid(files, context));
    }

    @Test
    void testIsInvalidWithWrongMimeType() {
        final MultipartFile[] files = new MultipartFile[] {
            new MockMultipartFile(
                "images",
                "fake.png",
                "application/pdf",
                "%PDF-1.4 not an image".getBytes(StandardCharsets.US_ASCII))
        };

        assertFalse(validator.isValid(files, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
        verify(builder).addConstraintViolation();
    }

    private static byte[] validPng() throws Exception {
        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", out));
        return out.toByteArray();
    }
}
