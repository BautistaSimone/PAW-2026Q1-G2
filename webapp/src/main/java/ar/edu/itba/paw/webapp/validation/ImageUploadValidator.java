package ar.edu.itba.paw.webapp.validation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.web.multipart.MultipartFile;

public final class ImageUploadValidator {

    public static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    public static final int MAX_IMAGES_PER_PRODUCT = 8;
    public static final long MAX_REQUEST_BYTES = MAX_IMAGE_BYTES * MAX_IMAGES_PER_PRODUCT;

    private static final int MAX_WIDTH = 8000;
    private static final int MAX_HEIGHT = 8000;
    private static final long MAX_PIXELS = 25_000_000L;

    private static final String JPEG_CONTENT_TYPE = "image/jpeg";
    private static final String PNG_CONTENT_TYPE = "image/png";
    private static final String WEBP_CONTENT_TYPE = "image/webp";

    private static final byte[] PNG_SIGNATURE = new byte[] {
        (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };

    private static final byte[] PNG_IEND = new byte[] {
        0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4e, 0x44,
        (byte) 0xae, 0x42, 0x60, (byte) 0x82
    };

    private ImageUploadValidator() {
        throw new AssertionError();
    }

    public static List<ValidatedImage> validateAll(final MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        final List<MultipartFile> presentFiles = new ArrayList<>();
        long totalSize = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            presentFiles.add(file);
            totalSize += file.getSize();
        }

        if (presentFiles.size() > MAX_IMAGES_PER_PRODUCT) {
            throw new InvalidImageUploadException("Podes cargar hasta " + MAX_IMAGES_PER_PRODUCT + " imagenes por producto.");
        }
        if (totalSize > MAX_REQUEST_BYTES) {
            throw new InvalidImageUploadException("El total de imagenes supera el limite permitido.");
        }

        final List<ValidatedImage> validated = new ArrayList<>(presentFiles.size());
        for (MultipartFile file : presentFiles) {
            validated.add(validate(file));
        }
        return validated;
    }

    public static ValidatedImage validate(final MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageUploadException("La imagen no puede estar vacia.");
        }
        if (file.getSize() <= 0 || file.getSize() > MAX_IMAGE_BYTES) {
            throw new InvalidImageUploadException("Cada imagen debe pesar como maximo 5 MB.");
        }

        final byte[] data = file.getBytes();
        if (data.length == 0 || data.length > MAX_IMAGE_BYTES) {
            throw new InvalidImageUploadException("Cada imagen debe pesar como maximo 5 MB.");
        }

        final ImageKind kind = validateImageData(data);
        return new ValidatedImage(data, kind.contentType);
    }

    public static Optional<String> detectSafeContentType(final byte[] data) {
        try {
            return Optional.of(validateImageData(data).contentType);
        } catch (InvalidImageUploadException | IOException e) {
            return Optional.empty();
        }
    }

    private static ImageKind validateImageData(final byte[] data) throws IOException {
        if (data == null || data.length == 0 || data.length > MAX_IMAGE_BYTES) {
            throw new InvalidImageUploadException("Cada imagen debe pesar como maximo 5 MB.");
        }

        final ImageKind kind = detectKind(data)
            .orElseThrow(() -> new InvalidImageUploadException("El archivo debe ser una imagen JPEG, PNG o WebP valida."));

        final Dimensions dimensions = kind == ImageKind.WEBP
            ? readWebpDimensions(data)
            : readImageIoDimensions(data, kind);

        validateDimensions(dimensions);
        return kind;
    }

    private static Optional<ImageKind> detectKind(final byte[] data) {
        if (isJpeg(data)) {
            return Optional.of(ImageKind.JPEG);
        }
        if (isPng(data)) {
            return Optional.of(ImageKind.PNG);
        }
        if (isWebp(data)) {
            return Optional.of(ImageKind.WEBP);
        }
        return Optional.empty();
    }

    private static boolean isJpeg(final byte[] data) {
        return data.length >= 4
            && (data[0] & 0xff) == 0xff
            && (data[1] & 0xff) == 0xd8
            && (data[2] & 0xff) == 0xff
            && (data[data.length - 2] & 0xff) == 0xff
            && (data[data.length - 1] & 0xff) == 0xd9;
    }

    private static boolean isPng(final byte[] data) {
        return startsWith(data, 0, PNG_SIGNATURE) && endsWith(data, PNG_IEND);
    }

    private static boolean isWebp(final byte[] data) {
        if (data.length < 30 || !startsWith(data, 0, "RIFF") || !startsWith(data, 8, "WEBP")) {
            return false;
        }
        final long riffSize = readUInt32LE(data, 4);
        if (riffSize != data.length - 8L) {
            return false;
        }
        return startsWith(data, 12, "VP8 ")
            || startsWith(data, 12, "VP8L")
            || startsWith(data, 12, "VP8X");
    }

    private static Dimensions readImageIoDimensions(final byte[] data, final ImageKind expectedKind) throws IOException {
        final ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
        if (input == null) {
            throw new InvalidImageUploadException("No pudimos leer la imagen.");
        }

        try (input) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new InvalidImageUploadException("El archivo no es una imagen valida.");
            }

            final ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                final String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!expectedKind.matchesImageIoFormat(format)) {
                    throw new InvalidImageUploadException("El formato real de la imagen no coincide con el contenido.");
                }
                return new Dimensions(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        }
    }

    private static Dimensions readWebpDimensions(final byte[] data) {
        final long chunkSize = readUInt32LE(data, 16);
        if (20L + chunkSize > data.length) {
            throw new InvalidImageUploadException("El archivo WebP no es valido.");
        }

        if (startsWith(data, 12, "VP8X")) {
            if (chunkSize < 10 || data.length < 30) {
                throw new InvalidImageUploadException("El archivo WebP no es valido.");
            }
            final int width = 1 + readUInt24LE(data, 24);
            final int height = 1 + readUInt24LE(data, 27);
            return new Dimensions(width, height);
        }

        if (startsWith(data, 12, "VP8L")) {
            if (chunkSize < 5 || data.length < 25 || (data[20] & 0xff) != 0x2f) {
                throw new InvalidImageUploadException("El archivo WebP no es valido.");
            }
            final int b1 = data[21] & 0xff;
            final int b2 = data[22] & 0xff;
            final int b3 = data[23] & 0xff;
            final int b4 = data[24] & 0xff;
            final int width = 1 + (((b2 & 0x3f) << 8) | b1);
            final int height = 1 + (((b4 & 0x0f) << 10) | (b3 << 2) | ((b2 & 0xc0) >> 6));
            return new Dimensions(width, height);
        }

        if (startsWith(data, 12, "VP8 ")) {
            if (chunkSize < 10 || data.length < 30
                || (data[23] & 0xff) != 0x9d
                || (data[24] & 0xff) != 0x01
                || (data[25] & 0xff) != 0x2a) {
                throw new InvalidImageUploadException("El archivo WebP no es valido.");
            }
            final int rawWidth = readUInt16LE(data, 26);
            final int rawHeight = readUInt16LE(data, 28);
            return new Dimensions(rawWidth & 0x3fff, rawHeight & 0x3fff);
        }

        throw new InvalidImageUploadException("El archivo WebP no es valido.");
    }

    private static void validateDimensions(final Dimensions dimensions) {
        if (dimensions.width <= 0 || dimensions.height <= 0) {
            throw new InvalidImageUploadException("La imagen tiene dimensiones invalidas.");
        }
        if (dimensions.width > MAX_WIDTH || dimensions.height > MAX_HEIGHT) {
            throw new InvalidImageUploadException("La imagen supera las dimensiones permitidas.");
        }
        if ((long) dimensions.width * (long) dimensions.height > MAX_PIXELS) {
            throw new InvalidImageUploadException("La imagen es demasiado grande para procesarla de forma segura.");
        }
    }

    private static boolean startsWith(final byte[] data, final int offset, final String expected) {
        return startsWith(data, offset, expected.getBytes(StandardCharsets.US_ASCII));
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

    private static boolean endsWith(final byte[] data, final byte[] expected) {
        if (data.length < expected.length) {
            return false;
        }
        return startsWith(data, data.length - expected.length, expected);
    }

    private static int readUInt16LE(final byte[] data, final int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
    }

    private static int readUInt24LE(final byte[] data, final int offset) {
        return (data[offset] & 0xff)
            | ((data[offset + 1] & 0xff) << 8)
            | ((data[offset + 2] & 0xff) << 16);
    }

    private static long readUInt32LE(final byte[] data, final int offset) {
        return ((long) data[offset] & 0xff)
            | (((long) data[offset + 1] & 0xff) << 8)
            | (((long) data[offset + 2] & 0xff) << 16)
            | (((long) data[offset + 3] & 0xff) << 24);
    }

    public static final class ValidatedImage {
        private final byte[] data;
        private final String contentType;

        private ValidatedImage(final byte[] data, final String contentType) {
            this.data = data;
            this.contentType = contentType;
        }

        public byte[] getData() {
            return data;
        }

        public String getContentType() {
            return contentType;
        }
    }

    public static class InvalidImageUploadException extends RuntimeException {
        public InvalidImageUploadException(final String message) {
            super(message);
        }
    }

    private enum ImageKind {
        JPEG(JPEG_CONTENT_TYPE) {
            @Override
            boolean matchesImageIoFormat(final String format) {
                return "jpeg".equals(format) || "jpg".equals(format);
            }
        },
        PNG(PNG_CONTENT_TYPE) {
            @Override
            boolean matchesImageIoFormat(final String format) {
                return "png".equals(format);
            }
        },
        WEBP(WEBP_CONTENT_TYPE) {
            @Override
            boolean matchesImageIoFormat(final String format) {
                return false;
            }
        };

        private final String contentType;

        ImageKind(final String contentType) {
            this.contentType = contentType;
        }

        abstract boolean matchesImageIoFormat(String format);
    }

    private static final class Dimensions {
        private final int width;
        private final int height;

        private Dimensions(final int width, final int height) {
            this.width = width;
            this.height = height;
        }
    }
}
