package ar.edu.itba.paw.webapp.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.webapp.form.ProductForm;

class ProductFormValidatorTest {

    private FakeImageService imageService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        imageService = new FakeImageService();
        final ValidatorFactory factory = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .constraintValidatorFactory(new ConstraintValidatorFactory() {
                @Override
                @SuppressWarnings("unchecked")
                public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
                    if (key.equals(ProductFormValidator.class)) {
                        return (T) new ProductFormValidator(imageService);
                    }
                    try {
                        return key.getDeclaredConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        throw new ValidationException(e);
                    }
                }

                @Override
                public void releaseInstance(final ConstraintValidator<?, ?> instance) {
                }
            })
            .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void rejectsCreationWithoutImages() {
        final ProductForm form = validForm();

        assertHasImageViolation(form);
    }

    @Test
    void rejectsEditWithoutPreviousImagesAndWithoutUploads() {
        final ProductForm form = validForm();
        form.setEditing(true);
        form.setProductId(1L);
        form.setHadExistingImages(false);

        assertHasImageViolation(form);
    }

    @Test
    void rejectsEditWithEmptyLayout() {
        final ProductForm form = validForm();
        form.setEditing(true);
        form.setProductId(1L);
        form.setHadExistingImages(true);
        form.setImageLayout("");

        assertHasImageViolation(form);
    }

    @Test
    void rejectsInvalidLayoutSyntax() {
        final ProductForm form = validForm();
        form.setEditing(true);
        form.setProductId(1L);
        form.setHadExistingImages(true);
        form.setImageLayout("e:1,bad-token");
        form.setImages(new MockMultipartFile[0]);

        assertHasImageViolation(form);
    }

    @Test
    void rejectsLayoutWhenNewSlotCountDiffersFromUploads() throws Exception {
        final ProductForm form = validForm();
        form.setEditing(true);
        form.setProductId(1L);
        form.setHadExistingImages(true);
        form.setImageLayout("e:1,n,n");
        form.setImages(new MockMultipartFile[] { imageFile("new.png") });

        assertHasImageViolation(form);
    }

    @Test
    void rejectsExistingImageThatDoesNotBelongToProduct() {
        final ProductForm form = validForm();
        form.setEditing(true);
        form.setProductId(1L);
        form.setHadExistingImages(true);
        form.setImageLayout("e:2");
        form.setImages(new MockMultipartFile[0]);
        imageService.setImages(List.of(new Image(1L, 1L, new byte[] { 1 }, "image/png")));

        assertHasImageViolation(form);
    }

    @Test
    void acceptsLayoutWithExistingAndNewImages() throws Exception {
        final ProductForm form = validForm();
        form.setEditing(true);
        form.setProductId(1L);
        form.setHadExistingImages(true);
        form.setImageLayout("e:1,n");
        form.setImages(new MockMultipartFile[] { imageFile("new.png") });
        imageService.setImages(List.of(new Image(1L, 1L, new byte[] { 1 }, "image/png")));

        assertTrue(validator.validate(form).isEmpty());
    }

    private void assertHasImageViolation(final ProductForm form) {
        final Set<ConstraintViolation<ProductForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(violation -> "images".equals(violation.getPropertyPath().toString())));
    }

    private static ProductForm validForm() {
        final ProductForm form = new ProductForm();
        form.setTitle("Title");
        form.setArtist("Artist");
        form.setRecordLabel("Label");
        form.setCatalogNumber("Catalog");
        form.setEditionCountry("Country");
        form.setCategories(List.of(1L));
        form.setSleeveCondition(BigDecimal.TEN);
        form.setRecordCondition(BigDecimal.TEN);
        form.setPrice(BigDecimal.ONE);
        form.setDescription("Description");
        form.setStock(1);
        form.setImages(new MockMultipartFile[0]);
        form.setEditing(false);
        form.setProductId(null);
        form.setHadExistingImages(false);
        return form;
    }

    private static MockMultipartFile imageFile(final String name) throws Exception {
        return new MockMultipartFile("images", name, "image/png", validPng());
    }

    private static byte[] validPng() throws Exception {
        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", out));
        return out.toByteArray();
    }

    private static final class FakeImageService implements ImageService {
        private List<Image> images = List.of();

        private void setImages(final List<Image> images) {
            this.images = images;
        }

        @Override
        public Image createImage(final Long productId, final byte[] data, final String contentType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Image> findById(final Long imageId) {
            return images.stream()
                .filter(image -> image.getImageId().equals(imageId))
                .findFirst();
        }

        @Override
        public Optional<Image> findByProductId(final Long productId) {
            return findAllByProductId(productId).stream().findFirst();
        }

        @Override
        public List<Image> findAllByProductId(final Long productId) {
            return images.stream()
                .filter(image -> image.getProductId().equals(productId))
                .collect(Collectors.toList());
        }

        @Override
        public boolean existsByProductId(final Long productId) {
            return !findAllByProductId(productId).isEmpty();
        }

        @Override
        public Set<Long> findProductIdsWithImages(final List<Long> productIds) {
            return images.stream()
                .map(Image::getProductId)
                .filter(productIds::contains)
                .collect(Collectors.toSet());
        }

        @Override
        public void deleteImagesByProductId(final Long productId) {
            throw new UnsupportedOperationException();
        }
    }
}
