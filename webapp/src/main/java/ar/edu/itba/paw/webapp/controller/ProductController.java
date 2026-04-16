package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.webapp.form.ProductForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
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


    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.findAll();
    }

    @RequestMapping(value = "/products/new", method = RequestMethod.GET)
    public ModelAndView newProductForm(@ModelAttribute("productForm") final ProductForm form) {
        return new ModelAndView("product-form");
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ModelAndView createProduct(
        @AuthenticationPrincipal PawAuthUser authUser,
        @Valid @ModelAttribute("productForm") final ProductForm form,
        final BindingResult errors
    ) throws IOException {

        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        if (errors.hasErrors()) {
            return new ModelAndView("product-form");
        }

        final MultipartFile[] images = form.getImages();
        // Validate images before creating the product
        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    if (image.getSize() > 5 * 1024 * 1024) {
                        errors.rejectValue("images", "Size.productForm.images", "La imagen no puede pesar más de 5MB.");
                        return new ModelAndView("product-form");
                    }
                    String contentType = image.getContentType();
                    if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
                        errors.rejectValue("images", "ContentType.productForm.images", "Formato de imagen inválido. Solo se admiten JPEG, PNG y WebP.");
                        return new ModelAndView("product-form");
                    }
                }
            }
        }

        // Get the current logged in user, return error if not found
        User user = authUser.getUser();

        final Product product = productService.createProduct(
            user.getId(),
            form.getTitle(),
            form.getArtist(),
            form.getRecordLabel(),
            form.getCatalogNumber(),
            form.getEditionCountry(),
            form.getCategories(),
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
    public ModelAndView productDetail(
        @PathVariable("id") final Long id,
        @ModelAttribute("purchaseCreateForm") final ar.edu.itba.paw.webapp.form.PurchaseCreateForm purchaseForm
    ) {
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

