package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;
import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.webapp.form.ImageForm;

@Controller
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(final ImageService imageService) {
        this.imageService = imageService;
    }

    // TODO: Implement
    @RequestMapping(value = "/images/{image_id}", method = RequestMethod.GET)
    public ModelAndView getImage() {
        return new ModelAndView("product-form");
    }

    @RequestMapping(value = "/images", method = RequestMethod.POST)
    public ModelAndView createImage(@ModelAttribute ImageForm form) {
        // TODO: Hacer con ModelMap
        // imageService.createImage(form.getProductId(), form.getImage().getBytes());
        return new ModelAndView("redirect:/?created=1");
    }
}
