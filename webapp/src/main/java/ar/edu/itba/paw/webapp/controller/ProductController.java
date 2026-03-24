package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.services.ProductService;

@Controller
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(final ProductService productService) {
        this.productService = productService;
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
        @RequestParam("vinylCondition") final String vinylCondition,
        @RequestParam("price") final BigDecimal price,
        @RequestParam(value = "imageUrl", required = false) final String imageUrl,
        @RequestParam(value = "description", required = false) final String description
    ) {
        productService.createProduct(title, artist, genre, vinylCondition, price, imageUrl, description);
        return new ModelAndView("redirect:/?created=1");
    }
}
