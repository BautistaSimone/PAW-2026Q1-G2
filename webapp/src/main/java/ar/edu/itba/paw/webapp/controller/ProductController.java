package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.webapp.form.ProductForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator.InvalidImageUploadException;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator.ValidatedImage;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductReportRemovalTokenService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final EmailService emailService;
    private final ProductReportRemovalTokenService reportRemovalTokenService;
    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ProductController(
        final ProductService productService,
        final CategoryService categoryService,
        final ImageService imageService,
        final EmailService emailService,
        final ProductReportRemovalTokenService reportRemovalTokenService,
        final ReviewService reviewService,
        final UserService userService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.emailService = emailService;
        this.reportRemovalTokenService = reportRemovalTokenService;
        this.reviewService = reviewService;
        this.userService = userService;
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
    ) {

        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        if (errors.hasErrors()) {
            return new ModelAndView("product-form");
        }

        final List<ValidatedImage> validatedImages;
        try {
            validatedImages = ImageUploadValidator.validateAll(form.getImages());
        } catch (InvalidImageUploadException e) {
            errors.rejectValue("images", "Invalid.productForm.images", e.getMessage());
            return new ModelAndView("product-form");
        } catch (IOException e) {
            errors.rejectValue("images", "Read.productForm.images", "No pudimos leer la imagen enviada.");
            return new ModelAndView("product-form");
        }

        // Get the current logged in user
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

        for (ValidatedImage image : validatedImages) {
            imageService.createImage(
                product.getId(),
                image.getData(),
                image.getContentType()
            );
        }

        return new ModelAndView("redirect:/products/" + product.getId() + "?created=1");
    }

    @RequestMapping(value = "/products/{id}", method = RequestMethod.GET)
    public ModelAndView productDetail(
        @PathVariable("id") final Long id,
        @ModelAttribute("purchaseCreateForm") final ar.edu.itba.paw.webapp.form.PurchaseCreateForm purchaseForm
    ) {
        final Product product = productService.findByIdIfAvailable(id)
            .orElseThrow(ResourceNotFoundException::new);

        final ModelAndView mav = new ModelAndView("product-detail");
        mav.addObject("product", product);

        final List<ar.edu.itba.paw.models.Image> productImages = imageService.findAllByProductId(product.getId());
        if (!productImages.isEmpty()) {
            mav.addObject("productImages", productImages);
            mav.addObject("productImageUrl", "/images/" + productImages.get(0).getImageId());
        }

        mav.addObject("sellerRating", reviewService.summaryForSeller(product.getUserId()));
        userService.findById(product.getUserId()).ifPresent(seller ->
            mav.addObject("seller", seller)
        );

        List<Product> sellerProducts = productService.listProducts(
            new ProductSearchCriteria(null, null, null, null, null, null, ProductSortOrder.NEWEST, product.getUserId())
        ).stream().filter(p -> !p.getId().equals(product.getId())).limit(10).collect(Collectors.toList());

        List<Product> relatedProducts = productService.listProducts(
            new ProductSearchCriteria(product.getArtist(), null, null, null, null, null, ProductSortOrder.NEWEST, null)
        ).stream().filter(p -> !p.getId().equals(product.getId())).limit(10).collect(Collectors.toList());

        if (relatedProducts.isEmpty()) {
            relatedProducts = productService.listProducts().stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .filter(p -> sellerProducts.stream().noneMatch(sp -> sp.getId().equals(p.getId())))
                .limit(10).collect(Collectors.toList());
        }

        mav.addObject("sellerProducts", sellerProducts);
        mav.addObject("relatedProducts", relatedProducts);

        return mav;
    }

    @RequestMapping(value = "/products/{id}/report", method = RequestMethod.POST)
    public ModelAndView reportProduct(
        @AuthenticationPrincipal final PawAuthUser authUser,
        @PathVariable("id") final Long id
    ) {
        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Product product = productService.findByIdIfAvailable(id)
            .orElseThrow(ResourceNotFoundException::new);

        emailService.sendProductReportEmail(product, authUser.getUser());
        return new ModelAndView("redirect:/products/" + id + "?reported=1");
    }

    @RequestMapping(value = "/products/{id}/moderate-hide", method = RequestMethod.GET)
    public ModelAndView moderateHideFromReportMail(
        @PathVariable("id") final Long id,
        @RequestParam("token") final String token
    ) {
        if (!reportRemovalTokenService.isValid(id, token)) {
            throw new IllegalArgumentException("Invalid or expired moderation link");
        }
        productService.hideProductFromCatalog(id);
        return redirectAfterModerationHide();
    }

    private static ModelAndView redirectAfterModerationHide() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return new ModelAndView("redirect:/?moderated=1");
        }
        return new ModelAndView("redirect:/login?moderated=1");
    }
}

