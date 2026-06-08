package ar.edu.itba.paw.webapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductImageLayoutParser;
import ar.edu.itba.paw.services.ProductImageLayoutParser.Slot;
import ar.edu.itba.paw.webapp.form.ProductForm;

public class ProductFormValidator implements ConstraintValidator<ValidProductForm, ProductForm> {

    private final ImageService imageService;

    @Autowired
    public ProductFormValidator(final ImageService imageService) {
        this.imageService = imageService;
    }

    @Override
    public void initialize(final ValidProductForm constraintAnnotation) {
    }

    @Override
    public boolean isValid(final ProductForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        boolean valid = true;

        try {
            ImageUploadValidator.validateAll(form.getImages());
        } catch (ImageUploadValidator.InvalidImageUploadException e) {
            reject(context, "images", e.getMessage());
            valid = false;
        } catch (IOException e) {
            reject(context, "images", "Read.productForm.images");
            valid = false;
        }

        if (!valid) {
            return false;
        }

        final int uploadedCount = countNonEmptyFiles(form.getImages());
        if (!form.isEditing() || !form.isHadExistingImages()) {
            if (uploadedCount == 0) {
                reject(context, "images", "Required.productForm.images");
                return false;
            }
            return true;
        }

        final String layout = form.getImageLayout();
        if (layout == null) {
            return true;
        }
        if (layout.isBlank()) {
            reject(context, "images", "Invalid.productForm.imageLayout");
            return false;
        }

        final List<Slot> slots;
        try {
            slots = ProductImageLayoutParser.parse(layout);
        } catch (IllegalArgumentException e) {
            reject(context, "images", "Invalid.productForm.imageLayout");
            return false;
        }

        if (slots.isEmpty() || slots.size() > ImageUploadValidator.MAX_IMAGES_PER_PRODUCT) {
            reject(context, "images", "Invalid.productForm.imageLayout");
            return false;
        }

        int newSlotCount = 0;
        final Set<Long> existingImageIds = new HashSet<>();
        for (Slot slot : slots) {
            if (slot.getKind() == ProductImageLayoutParser.SlotKind.NEW) {
                newSlotCount++;
            } else {
                existingImageIds.add(slot.getExistingImageId());
            }
        }

        if (newSlotCount != uploadedCount) {
            reject(context, "images", "Invalid.productForm.imageLayout");
            return false;
        }

        if (!existingImageIds.isEmpty() && !existingImagesBelongToProduct(form.getProductId(), existingImageIds)) {
            reject(context, "images", "Invalid.productForm.imageLayout");
            return false;
        }

        return true;
    }

    private boolean existingImagesBelongToProduct(final Long productId, final Set<Long> imageIds) {
        if (productId == null) {
            return false;
        }
        final Set<Long> allowedImageIds = new HashSet<>();
        for (Image image : imageService.findAllByProductId(productId)) {
            allowedImageIds.add(image.getImageId());
        }
        return allowedImageIds.containsAll(imageIds);
    }

    private static int countNonEmptyFiles(final MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return 0;
        }
        int count = 0;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static void reject(
            final ConstraintValidatorContext context,
            final String property,
            final String messageTemplate) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate)
               .addPropertyNode(property)
               .addConstraintViolation();
    }
}
