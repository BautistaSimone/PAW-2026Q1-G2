package ar.edu.itba.paw.webapp.validation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public final class PaymentProofValidator {

    public static final long MAX_PROOF_BYTES = 5L * 1024L * 1024L;
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private static final byte[] PDF_HEADER = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PDF_EOF = "%%EOF".getBytes(StandardCharsets.US_ASCII);

    private PaymentProofValidator() {
        throw new AssertionError();
    }

    public static ValidatedPaymentProof validate(final MultipartFile file) throws InvalidPaymentProofException {
        if (file == null || file.isEmpty()) {
            throw new InvalidPaymentProofException("PurchaseForm.proof.required");
        }
        if (file.getSize() <= 0 || file.getSize() > MAX_PROOF_BYTES) {
            throw new InvalidPaymentProofException("PurchaseForm.proof.size");
        }

        final byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new InvalidPaymentProofException("PurchaseForm.proof.invalid");
        }

        if (data.length == 0 || data.length > MAX_PROOF_BYTES) {
            throw new InvalidPaymentProofException("PurchaseForm.proof.size");
        }

        if (isPdf(data)) {
            final String fileName = safeFileName(file.getOriginalFilename(), PDF_CONTENT_TYPE);
            return new ValidatedPaymentProof(data, PDF_CONTENT_TYPE, fileName);
        }

        try {
            final ImageUploadValidator.ValidatedImage validated = ImageUploadValidator.validateStoredImageBytes(data);
            final String fileName = safeFileName(file.getOriginalFilename(), validated.getContentType());
            return new ValidatedPaymentProof(data, validated.getContentType(), fileName);
        } catch (ImageUploadValidator.InvalidImageUploadException | IOException e) {
            throw new InvalidPaymentProofException("PurchaseForm.proof.invalid");
        }
    }

    public static Optional<String> detectSafeContentType(final byte[] data) {
        if (data == null || data.length == 0 || data.length > MAX_PROOF_BYTES) {
            return Optional.empty();
        }
        if (isPdf(data)) {
            return Optional.of(PDF_CONTENT_TYPE);
        }
        return ImageUploadValidator.detectSafeContentType(data);
    }

    public static String safeFileName(final String rawName) {
        return safeFileName(rawName, null);
    }

    private static String safeFileName(final String rawName, final String contentType) {
        String name = rawName == null ? "" : new File(rawName).getName().trim();
        if (name.isEmpty()) {
            name = defaultFileName(contentType);
        }

        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.length() > 80) {
            name = name.substring(0, 80);
        }
        return name;
    }

    private static String defaultFileName(final String contentType) {
        if (contentType == null) {
            return "payment-proof";
        }
        final String ct = contentType.toLowerCase(Locale.ROOT);
        if (ct.contains("pdf")) {
            return "payment-proof.pdf";
        }
        if (ct.contains("png")) {
            return "payment-proof.png";
        }
        if (ct.contains("webp")) {
            return "payment-proof.webp";
        }
        return "payment-proof.jpg";
    }

    private static boolean isPdf(final byte[] data) {
        if (data.length < PDF_HEADER.length + PDF_EOF.length) {
            return false;
        }
        if (!startsWith(data, 0, PDF_HEADER)) {
            return false;
        }
        return containsEofMarker(data);
    }

    private static boolean containsEofMarker(final byte[] data) {
        final int searchWindow = Math.min(data.length, 1024);
        final int start = data.length - searchWindow;
        for (int i = start; i <= data.length - PDF_EOF.length; i++) {
            if (startsWith(data, i, PDF_EOF)) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWith(final byte[] data, final int offset, final byte[] expected) {
        if (offset < 0 || data.length - offset < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public static final class ValidatedPaymentProof {
        private final byte[] data;
        private final String contentType;
        private final String fileName;

        public ValidatedPaymentProof(final byte[] data, final String contentType, final String fileName) {
            this.data = data;
            this.contentType = contentType;
            this.fileName = fileName;
        }

        public byte[] getData() {
            return data;
        }

        public String getContentType() {
            return contentType;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static final class InvalidPaymentProofException extends RuntimeException {
        private final String messageKey;

        public InvalidPaymentProofException(final String messageKey) {
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }
}
