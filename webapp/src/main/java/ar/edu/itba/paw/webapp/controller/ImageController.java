package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.webapp.form.ImageForm;

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

    @RequestMapping(value = "/images", method = RequestMethod.POST)
    public ModelAndView createImage(@ModelAttribute final ImageForm form) throws IOException {
        if (form.getProductId() != null && form.getImage() != null && !form.getImage().isEmpty()) {
            imageService.createImage(
                form.getProductId(),
                form.getImage().getBytes(),
                form.getImage().getContentType()
            );
        }

        return new ModelAndView("redirect:/?created=1");
    }

    private ResponseEntity<byte[]> buildImageResponse(final Optional<Image> image) {
        if (image.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final Image storedImage = image.get();
        final MediaType contentType = resolveContentType(storedImage.getContentType());

        return ResponseEntity.ok()
            .contentType(contentType)
            .contentLength(storedImage.getData().length)
            .body(storedImage.getData());
    }

    private MediaType resolveContentType(final String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
