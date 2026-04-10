package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;

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
    public ModelAndView newProductForm() {
        final ModelAndView mav = new ModelAndView("product-form");
        mav.addObject("categories", categoryService.findAll());
        return mav;
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ModelAndView createProduct(
        @RequestParam("sellerEmail") final String sellerEmail,
        @RequestParam("title") final String title,
        @RequestParam("artist") final String artist,
        @RequestParam("recordLabel") final String recordLabel,
        @RequestParam("catalogNumber") final String catalogNumber,
        @RequestParam("editionCountry") final String editionCountry,
        @RequestParam(value = "categories", required = false) final List<Long> categoryIds,
        @RequestParam("description") final String description,
        @RequestParam("sleeveCondition") final BigDecimal sleeveCondition,
        @RequestParam("recordCondition") final BigDecimal recordCondition,
        @RequestParam("neighborhood") final String neighborhood,
        @RequestParam("province") final String province,
        @RequestParam("price") final BigDecimal price,
        @RequestParam(value = "images", required = false) final MultipartFile[] images
    ) throws IOException {

        // Validate images before creating the product
        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    if (image.getSize() > 5 * 1024 * 1024) {
                        throw new IllegalArgumentException("Image size must be less than 5MB");
                    }
                    String contentType = image.getContentType();
                    if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
                        throw new IllegalArgumentException("Invalid image content type. Only JPEG, PNG, and WebP are allowed.");
                    }
                }
            }
        }

        final Product product = productService.createProduct(
            sellerEmail,
            title,
            artist,
            recordLabel,
            catalogNumber,
            editionCountry,
            categoryIds,
            description,
            sleeveCondition,
            recordCondition,
            neighborhood,
            province,
            price
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

