package ar.edu.itba.paw.webapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.webapp.form.ProductForm;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser.Slot;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser.SlotKind;
import ar.edu.itba.paw.services.ImageService;

public class ProductFormValidator implements ConstraintValidator<ValidProductForm, ProductForm> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ImageService imageService;

    @Override
    public void initialize(final ValidProductForm constraintAnnotation) {
    }

    @Override
    public boolean isValid(final ProductForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        final Long editProductId = getProductIdFromRequest();
        final boolean isEditing = (editProductId != null);

        if (!isEditing) {
            // Creation validation: requires at least one image, and all must be valid
            final List<MultipartFile> presentFiles = extractNonEmptyMultipartFilesList(form.getImages());
            if (presentFiles.isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Required.productForm.images")
                       .addPropertyNode("images")
                       .addConstraintViolation();
                return false;
            }

            try {
                ImageUploadValidator.validateAll(form.getImages());
            } catch (ImageUploadValidator.InvalidImageUploadException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(e.getMessage())
                       .addPropertyNode("images")
                       .addConstraintViolation();
                return false;
            } catch (IOException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Read.productForm.images")
                       .addPropertyNode("images")
                       .addConstraintViolation();
                return false;
            }
        } else {
            // Edit validation
            final boolean hadImages = imageService.existsByProductId(editProductId);
            final String layoutRaw = form.getImageLayout();
            final boolean useLayout = hadImages && layoutRaw != null && !layoutRaw.isBlank();

            if (!hadImages) {
                final List<MultipartFile> presentFiles = extractNonEmptyMultipartFilesList(form.getImages());
                if (presentFiles.isEmpty()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Required.productForm.images")
                           .addPropertyNode("images")
                           .addConstraintViolation();
                    return false;
                }
                try {
                    ImageUploadValidator.validateAll(form.getImages());
                } catch (ImageUploadValidator.InvalidImageUploadException e) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(e.getMessage())
                           .addPropertyNode("images")
                           .addConstraintViolation();
                    return false;
                } catch (IOException e) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Read.productForm.images")
                           .addPropertyNode("images")
                           .addConstraintViolation();
                    return false;
                }
            } else if (useLayout) {
                final List<Slot> slots;
                try {
                    slots = ProductImageLayoutParser.parse(layoutRaw);
                } catch (RuntimeException ex) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Invalid.productForm.imageLayout")
                           .addPropertyNode("images")
                           .addConstraintViolation();
                    return false;
                }
                if (slots.isEmpty() || slots.size() > ImageUploadValidator.MAX_IMAGES_PER_PRODUCT) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Invalid.productForm.imageLayout")
                           .addPropertyNode("images")
                           .addConstraintViolation();
                    return false;
                }
                final long newSlotCount = slots.stream().filter(s -> s.getKind() == SlotKind.NEW).count();
                final List<MultipartFile> newFiles = extractNonEmptyMultipartFilesList(form.getImages());
                if (newSlotCount != newFiles.size()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Invalid.productForm.imageLayout")
                           .addPropertyNode("images")
                           .addConstraintViolation();
                    return false;
                }
                long newBytesTotal = 0;
                for (MultipartFile f : newFiles) {
                    newBytesTotal += f.getSize();
                }
                if (newBytesTotal > ImageUploadValidator.MAX_REQUEST_BYTES) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Invalid.productForm.images")
                           .addPropertyNode("images")
                           .addConstraintViolation();
                    return false;
                }

                int newFileIndex = 0;
                for (final Slot slot : slots) {
                    if (slot.getKind() == SlotKind.EXISTING) {
                        final Optional<Image> imgOpt = imageService.findById(slot.getExistingImageId());
                        if (imgOpt.isEmpty() || !imgOpt.get().getProductId().equals(editProductId)) {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate("Invalid.productForm.imageLayout")
                                   .addPropertyNode("images")
                                   .addConstraintViolation();
                            return false;
                        }
                        try {
                            ImageUploadValidator.validateStoredImageBytes(imgOpt.get().getData());
                        } catch (ImageUploadValidator.InvalidImageUploadException e) {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate(e.getMessage())
                                   .addPropertyNode("images")
                                   .addConstraintViolation();
                            return false;
                        } catch (IOException e) {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate("Read.productForm.images")
                                   .addPropertyNode("images")
                                   .addConstraintViolation();
                            return false;
                        }
                    } else {
                        try {
                            ImageUploadValidator.validate(newFiles.get(newFileIndex++));
                        } catch (ImageUploadValidator.InvalidImageUploadException e) {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate(e.getMessage())
                                   .addPropertyNode("images")
                                   .addConstraintViolation();
                            return false;
                        } catch (IOException e) {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate("Read.productForm.images")
                                   .addPropertyNode("images")
                                   .addConstraintViolation();
                            return false;
                        }
                    }
                }
            } else {
                final boolean hasNewImages = hasNonEmptyMultipartFiles(form.getImages());
                if (hasNewImages) {
                    try {
                        ImageUploadValidator.validateAll(form.getImages());
                    } catch (ImageUploadValidator.InvalidImageUploadException e) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(e.getMessage())
                               .addPropertyNode("images")
                               .addConstraintViolation();
                        return false;
                    } catch (IOException e) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate("Read.productForm.images")
                               .addPropertyNode("images")
                               .addConstraintViolation();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private Long getProductIdFromRequest() {
        if (request == null) {
            return null;
        }
        final String uri = request.getRequestURI();
        final Pattern p = Pattern.compile("/products/(\\d+)/edit");
        final Matcher m = p.matcher(uri);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return null;
    }

    private static List<MultipartFile> extractNonEmptyMultipartFilesList(final MultipartFile[] files) {
        if (files == null) {
            return List.of();
        }
        final List<MultipartFile> out = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                out.add(f);
            }
        }
        return out;
    }

    private static boolean hasNonEmptyMultipartFiles(final MultipartFile[] files) {
        if (files == null) {
            return false;
        }
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
