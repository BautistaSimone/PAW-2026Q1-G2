package ar.edu.itba.paw.webapp.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator;

@Controller
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(final ImageService imageService) {
        this.imageService = imageService;
    }

    @RequestMapping(value = "/images/{imageId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable("imageId") final Long imageId) {
        return buildImageResponse(imageService.findById(imageId));
    }

    @RequestMapping(value = "/images/product/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable("productId") final Long productId) {
        return buildImageResponse(imageService.findByProductId(productId));
    }

    private ResponseEntity<byte[]> buildImageResponse(final Optional<Image> image) {
        if (image.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final Image storedImage = image.get();
        final Optional<String> safeContentType = ImageUploadValidator.detectSafeContentType(storedImage.getData());
        if (safeContentType.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.parseMediaType(safeContentType.get()))
            .contentLength(storedImage.getData().length)
            .body(storedImage.getData());
    }
}
