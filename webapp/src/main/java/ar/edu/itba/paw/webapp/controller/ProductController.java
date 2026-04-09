package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.webapp.form.ProductForm;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;

    @Autowired
    public ProductController(
        final ProductService productService,
        final CategoryService categoryService,
        final ImageService imageService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
    }


    @RequestMapping(value = "/products/new", method = RequestMethod.GET)
    public ModelAndView newProductForm(@ModelAttribute("productForm") final ProductForm form) {
        final ModelAndView mav = new ModelAndView("product-form");
        mav.addObject("categories", categoryService.findAll());
        return mav;
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ModelAndView createProduct(
        @Valid // Check that the input values are valid
        @ModelAttribute("productForm") final ProductForm form,
        @RequestParam(value = "images", required = false) final MultipartFile[] images,
        final BindingResult errors
    ) throws IOException {

        if (errors.hasErrors()) {
            return newProductForm(form);    // Return to the form with the previous values
        }

        final Product product = productService.createProduct(
            form.getSellerEmail(),
            form.getTitle(),
            form.getArtist(),
            form.getRecordLabel(),
            form.getCatalogNumber(),
            form.getEditionCountry(),
            form.getCategoryIds(),
            form.getDescription(),
            form.getSleeveCondition(),
            form.getRecordCondition(),
            form.getNeighborhood(),
            form.getProvince(),
            form.getPrice()
        );

        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    imageService.createImage(
                        product.getId(),
                        image.getBytes(),
                        image.getContentType()
                    );
                }
            }
        }

        return new ModelAndView("redirect:/products/" + product.getId() + "?created=1");
    }

    @RequestMapping(value = "/products/{id}", method = RequestMethod.GET)
    public ModelAndView productDetail(@PathVariable("id") final Long id) {
        final Product product = productService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        final ModelAndView mav = new ModelAndView("product-detail");
        mav.addObject("product", product);

        final List<ar.edu.itba.paw.models.Image> productImages = imageService.findAllByProductId(product.getId());
        if (!productImages.isEmpty()) {
            mav.addObject("productImages", productImages);
            mav.addObject("productImageUrl", "/images/" + productImages.get(0).getImageId());
        }

        return mav;
    }
}

