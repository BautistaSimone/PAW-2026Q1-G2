package ar.edu.itba.paw.webapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import ar.edu.itba.paw.webapp.form.ProductForm;

public class ProductFormValidator implements ConstraintValidator<ValidProductForm, ProductForm> {

    @Override
    public void initialize(final ValidProductForm constraintAnnotation) {
    }

    @Override
    public boolean isValid(final ProductForm form, final ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        // Only validate file-level constraints: size, MIME type, emptiness
        // Creation vs edit logic and DB lookups are handled by the controller
        final MultipartFile[] files = form.getImages();
        if (files == null || files.length == 0) {
            return true;
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

        return true;
    }
}