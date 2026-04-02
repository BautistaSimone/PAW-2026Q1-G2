package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.UserService;

@Controller
public class ProductController {

    private static final String TEST_SELLER_EMAIL = "seller.test@vinyland.local";
    private static final String TEST_SELLER_PASSWORD = "temporary-password";
    private static final String TEST_SELLER_USERNAME = "Vendedor de prueba";

    private final ProductService productService;
    private final ImageService imageService;
    private final UserService userService;

    @Autowired
    public ProductController(
        final ProductService productService,
        final ImageService imageService,
        final UserService userService
    ) {
        this.productService = productService;
        this.imageService = imageService;
        this.userService = userService;
    }

    @RequestMapping(value = "/products/new", method = RequestMethod.GET)
    public ModelAndView newProductForm() {
        return new ModelAndView("product-form");
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ModelAndView createProduct(
        @RequestParam("title") final String title,
        @RequestParam("artist") final String artist,
        @RequestParam("genre") final String genre,
        @RequestParam("description") final String description,
        @RequestParam("condition") final String condition,
        @RequestParam("price") final BigDecimal price,
        @RequestParam(value = "image", required = false) final MultipartFile image
    ) throws IOException {
        final Long sellerId = userService.findByEmail(TEST_SELLER_EMAIL)
            .orElseGet(() -> userService.createUser(
                TEST_SELLER_EMAIL,
                TEST_SELLER_PASSWORD,
                TEST_SELLER_USERNAME,
                false
            ))
            .getId();

        final Product product = productService.createProduct(
            sellerId,
            title,
            artist,
            genre,
            description,
            condition,
            price
        );

        if (image != null && !image.isEmpty()) {
            imageService.createImage(
                product.getId(),
                image.getBytes(),
                image.getContentType()
            );
        }

        return new ModelAndView("redirect:/?created=1");
    }
}
