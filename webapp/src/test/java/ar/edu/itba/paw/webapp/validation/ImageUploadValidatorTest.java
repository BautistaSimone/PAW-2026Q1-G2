package ar.edu.itba.paw.webapp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import ar.edu.itba.paw.webapp.validation.ImageUploadValidator.InvalidImageUploadException;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator.ValidatedImage;

class ImageUploadValidatorTest {

    @Test
    void rejectsHtmlEvenIfClientClaimsPng() {
        final MockMultipartFile file = new MockMultipartFile(
            "images",
            "attack.png",
            "image/png",
            "<script>alert('xss')</script>".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(InvalidImageUploadException.class, () -> ImageUploadValidator.validate(file));
    }

    @Test
    void rejectsPdfEvenIfClientClaimsPng() {
        final MockMultipartFile file = new MockMultipartFile(
            "images",
            "fake.png",
            "image/png",
            "%PDF-1.4 fake pdf".getBytes(StandardCharsets.US_ASCII)
        );

        assertThrows(InvalidImageUploadException.class, () -> ImageUploadValidator.validate(file));
    }

    @Test
    void acceptsValidPngAndStoresCanonicalContentType() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
            "images",
            "cover.bin",
            "text/html",
            validPng()
        );

        final ValidatedImage image = ImageUploadValidator.validate(file);

        assertEquals("image/png", image.getContentType());
    }

    @Test
    void rejectsTooManyImages() throws Exception {
        final MockMultipartFile[] files = new MockMultipartFile[ImageUploadValidator.MAX_IMAGES_PER_PRODUCT + 1];
        final byte[] imageData = validPng();
        for (int i = 0; i < files.length; i++) {
            files[i] = new MockMultipartFile("images", "cover-" + i + ".png", "image/png", imageData);
        }

        assertThrows(InvalidImageUploadException.class, () -> ImageUploadValidator.validateAll(files));
    }

    @Test
    void safeContentTypeDetectionRejectsUnsafeStoredBytes() {
        assertTrue(ImageUploadValidator.detectSafeContentType(
            "<html>not an image</html>".getBytes(StandardCharsets.UTF_8)
        ).isEmpty());
    }

    private static byte[] validPng() throws Exception {
        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", out));
        return out.toByteArray();
    }
}
