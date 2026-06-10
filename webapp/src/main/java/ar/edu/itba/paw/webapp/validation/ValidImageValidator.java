package ar.edu.itba.paw.webapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public class ValidImageValidator implements ConstraintValidator<ValidImage, MultipartFile[]> {

    @Override
    public void initialize(ValidImage constraintAnnotation) {
    }

    @Override
    public boolean isValid(MultipartFile[] files, ConstraintValidatorContext context) {
        if (files == null || files.length == 0) {
            return true;
        }
        try {
            ImageUploadValidator.validateAll(files);
            return true;
        } catch (ImageUploadValidator.InvalidImageUploadException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        } catch (IOException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{Read.productForm.images}").addConstraintViolation();
            return false;
        }
    }
}
