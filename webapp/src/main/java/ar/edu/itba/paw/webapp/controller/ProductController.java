package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;
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
import ar.edu.itba.paw.services.ReportService;
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
    private final ReportService reportService;
    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ProductController(
        final ProductService productService,
        final CategoryService categoryService,
        final ImageService imageService,
        final EmailService emailService,
        final ProductReportRemovalTokenService reportRemovalTokenService,
        final ReportService reportService,
        final ReviewService reviewService,
        final UserService userService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.emailService = emailService;
        this.reportRemovalTokenService = reportRemovalTokenService;
        this.reportService = reportService;
        this.reviewService = reviewService;
        this.userService = userService;
    }


    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.findAll();
    }

    @RequestMapping(value = "/products/new", method = RequestMethod.GET)
    public ModelAndView newProductForm(
        @AuthenticationPrincipal final PawAuthUser authUser,
        @ModelAttribute("productForm") final ProductForm form
    ) {
        return redirectIfCannotPublish(authUser).orElseGet(this::productFormView);
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ModelAndView createProduct(
        @AuthenticationPrincipal PawAuthUser authUser,
        @Valid @ModelAttribute("productForm") final ProductForm form,
        final BindingResult errors
    ) {

        final Optional<ModelAndView> publishGuard = redirectIfCannotPublish(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        if (errors.hasErrors()) {
            return productFormView();
        }

        final List<ValidatedImage> validatedImages;
        try {
            validatedImages = ImageUploadValidator.validateAll(form.getImages());
        } catch (InvalidImageUploadException e) {
            errors.rejectValue("images", "Invalid.productForm.images", e.getMessage());
            return productFormView();
        } catch (IOException e) {
            errors.rejectValue("images", "Read.productForm.images", null);
            return productFormView();
        }

        if (validatedImages.isEmpty()) {
            errors.rejectValue("images", "Required.productForm.images", null);
            return productFormView();
        }

        final User publisher = userService.findById(authUser.getUser().getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));

        final Product product = productService.createProduct(
            publisher.getId(),
            form.getTitle(),
            form.getArtist(),
            form.getRecordLabel(),
            form.getCatalogNumber(),
            form.getEditionCountry(),
            form.getCategories(),
            form.getDescription(),
            form.getSleeveCondition(),
            form.getRecordCondition(),
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

    @RequestMapping(value = "/products/{id}/edit", method = RequestMethod.GET)
    public ModelAndView editProductForm(
        @AuthenticationPrincipal final PawAuthUser authUser,
        @PathVariable("id") final Long id,
        @ModelAttribute("productForm") final ProductForm form
    ) {
        final Optional<ModelAndView> publishGuard = redirectIfCannotPublish(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        final Product product = productService.findByIdIfAvailable(id)
            .orElseThrow(ResourceNotFoundException::new);

        if (!product.getUserId().equals(authUser.getUser().getId())) {
            throw new ResourceNotFoundException();
        }

        form.setTitle(product.getTitle());
        form.setArtist(product.getArtist());
        form.setRecordLabel(product.getRecordLabel());
        form.setCatalogNumber(product.getCatalogNumber());
        form.setEditionCountry(product.getEditionCountry());
        form.setDescription(product.getDescription());
        form.setSleeveCondition(product.getSleeveCondition());
        form.setRecordCondition(product.getRecordCondition());
        form.setPrice(product.getPrice());
        form.setCategories(
            product.getCategories().stream().map(c -> c.getId()).collect(Collectors.toList())
        );

        final ModelAndView mav = new ModelAndView("product-form");
        mav.addObject("isEditing", Boolean.TRUE);
        mav.addObject("editingProductId", id);
        mav.addObject("hasExistingProductImages", imageService.existsByProductId(id));
        attachProductFormSuggestions(mav);
        return mav;
    }

    @RequestMapping(value = "/products/{id}/edit", method = RequestMethod.POST)
    public ModelAndView updateProduct(
        @AuthenticationPrincipal final PawAuthUser authUser,
        @PathVariable("id") final Long id,
        @Valid @ModelAttribute("productForm") final ProductForm form,
        final BindingResult errors
    ) {
        final Optional<ModelAndView> publishGuard = redirectIfCannotPublish(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        final Product existing = productService.findByIdIfAvailable(id)
            .orElseThrow(ResourceNotFoundException::new);

        if (!existing.getUserId().equals(authUser.getUser().getId())) {
            throw new ResourceNotFoundException();
        }

        if (errors.hasErrors()) {
            final ModelAndView mav = new ModelAndView("product-form");
            mav.addObject("isEditing", Boolean.TRUE);
            mav.addObject("editingProductId", id);
            mav.addObject("hasExistingProductImages", imageService.existsByProductId(id));
            attachProductFormSuggestions(mav);
            return mav;
        }

        final boolean hasNewImages = hasNonEmptyMultipartFiles(form.getImages());
        final List<ValidatedImage> validatedImages = new ArrayList<>();
        if (hasNewImages) {
            try {
                validatedImages.addAll(ImageUploadValidator.validateAll(form.getImages()));
            } catch (InvalidImageUploadException e) {
                errors.rejectValue("images", "Invalid.productForm.images", e.getMessage());
                final ModelAndView mav = new ModelAndView("product-form");
                mav.addObject("isEditing", Boolean.TRUE);
                mav.addObject("editingProductId", id);
                mav.addObject("hasExistingProductImages", imageService.existsByProductId(id));
                attachProductFormSuggestions(mav);
                return mav;
            } catch (IOException e) {
                errors.rejectValue("images", "Read.productForm.images", null);
                final ModelAndView mav = new ModelAndView("product-form");
                mav.addObject("isEditing", Boolean.TRUE);
                mav.addObject("editingProductId", id);
                mav.addObject("hasExistingProductImages", imageService.existsByProductId(id));
                attachProductFormSuggestions(mav);
                return mav;
            }
            if (validatedImages.isEmpty()) {
                errors.rejectValue("images", "Required.productForm.images", null);
                final ModelAndView mav = new ModelAndView("product-form");
                mav.addObject("isEditing", Boolean.TRUE);
                mav.addObject("editingProductId", id);
                mav.addObject("hasExistingProductImages", imageService.existsByProductId(id));
                attachProductFormSuggestions(mav);
                return mav;
            }
        } else if (!imageService.existsByProductId(id)) {
            errors.rejectValue("images", "Required.productForm.images", null);
            final ModelAndView mav = new ModelAndView("product-form");
            mav.addObject("isEditing", Boolean.TRUE);
            mav.addObject("editingProductId", id);
            mav.addObject("hasExistingProductImages", Boolean.FALSE);
            attachProductFormSuggestions(mav);
            return mav;
        }

        productService.updateProduct(
            authUser.getUser().getId(),
            id,
            form.getTitle(),
            form.getArtist(),
            form.getRecordLabel(),
            form.getCatalogNumber(),
            form.getEditionCountry(),
            form.getCategories(),
            form.getDescription(),
            form.getSleeveCondition(),
            form.getRecordCondition(),
            form.getPrice()
        );

        if (hasNewImages) {
            imageService.deleteImagesByProductId(id);
            for (ValidatedImage image : validatedImages) {
                imageService.createImage(id, image.getData(), image.getContentType());
            }
        }

        return new ModelAndView("redirect:/products/" + id + "?updated=1");
    }

    @RequestMapping(value = "/products/{id}/restore", method = RequestMethod.POST)
    public ModelAndView restoreDeletedProduct(
        @AuthenticationPrincipal final PawAuthUser authUser,
        @PathVariable("id") final Long id
    ) {
        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (!productService.restoreUserDeletedProduct(id, authUser.getUser().getId())) {
            return new ModelAndView("redirect:/profile/trash?restoreError=1");
        }
        return new ModelAndView("redirect:/profile/trash?restored=1");
    }

    private static boolean hasNonEmptyMultipartFiles(final MultipartFile[] files) {
        if (files == null) {
            return false;
        }
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/products/{id}", method = RequestMethod.GET)
    public ModelAndView productDetail(
        @PathVariable("id") final Long id,
        @AuthenticationPrincipal final PawAuthUser authUser,
        @ModelAttribute("purchaseCreateForm") final ar.edu.itba.paw.webapp.form.PurchaseCreateForm purchaseForm
    ) {
        final Product product = productService.findByIdIfAvailable(id)
            .orElseThrow(ResourceNotFoundException::new);

        final ModelAndView mav = new ModelAndView("product-detail");
        mav.addObject("product", product);

        final boolean isOwnProduct = authUser != null
            && product.getUserId().equals(authUser.getUser().getId());
        mav.addObject("isOwnProduct", isOwnProduct);

        final List<ar.edu.itba.paw.models.Image> productImages = imageService.findAllByProductId(product.getId());
        if (!productImages.isEmpty()) {
            mav.addObject("productImages", productImages);
            mav.addObject("productImageUrl", "/images/" + productImages.get(0).getImageId());
        }

        mav.addObject("sellerRating", reviewService.summaryForSeller(product.getUserId()));
        userService.findById(product.getUserId()).ifPresent(seller ->
            mav.addObject("seller", seller)
        );
        mav.addObject("sellerReviews", reviewService.findBySellerId(product.getUserId(), 1, 3).getResults());

        List<Product> sellerProducts = productService.listProducts(
            new ProductSearchCriteria(null, null, null, null, null, null, ProductSortOrder.NEWEST, product.getUserId(), 1, 11)
        ).getResults().stream().filter(p -> !p.getId().equals(product.getId())).limit(10).collect(Collectors.toList());

        List<Product> relatedProducts = productService.listProducts(
            new ProductSearchCriteria(product.getArtist(), null, null, null, null, null, ProductSortOrder.NEWEST, null, 1, 11)
        ).getResults().stream().filter(p -> !p.getId().equals(product.getId())).limit(10).collect(Collectors.toList());

        if (relatedProducts.isEmpty()) {
            relatedProducts = productService.listProducts().getResults().stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .filter(p -> sellerProducts.stream().noneMatch(sp -> sp.getId().equals(p.getId())))
                .limit(10).collect(Collectors.toList());
        }

        final Set<Long> carouselSellerIds = new HashSet<>();
        for (Product p : sellerProducts) {
            carouselSellerIds.add(p.getUserId());
        }
        for (Product p : relatedProducts) {
            carouselSellerIds.add(p.getUserId());
        }
        final Map<Long, SellerRatingSummary> sellerRatings = new HashMap<>();
        for (Long uid : carouselSellerIds) {
            sellerRatings.put(uid, reviewService.summaryForSeller(uid));
        }

        mav.addObject("sellerProducts", sellerProducts);
        mav.addObject("relatedProducts", relatedProducts);
        mav.addObject("sellerRatings", sellerRatings);

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

        if (reportService.hasReported(id, authUser.getUser().getId())) {
            return new ModelAndView("redirect:/products/" + id + "?alreadyReported=1");
        }

        reportService.report(id, authUser.getUser().getId());

        User seller = userService.findById(product.getUserId())
            .orElseThrow(ResourceNotFoundException::new);

        emailService.sendProductReportEmail(product, authUser.getUser(), seller);
        return new ModelAndView("redirect:/products/" + id + "?reported=1");
    }

    @RequestMapping(value = "/products/{id}/delete", method = RequestMethod.POST)
    public ModelAndView deleteOwnProduct(
        @AuthenticationPrincipal final PawAuthUser authUser,
        @PathVariable("id") final Long id
    ) {
        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Product product = productService.findById(id)
            .orElseThrow(ResourceNotFoundException::new);

        if (!product.getUserId().equals(authUser.getUser().getId())) {
            return new ModelAndView("redirect:/profile?deleteError=forbidden");
        }

        if (!productService.hideProductByUser(id, authUser.getUser().getId())) {
            return new ModelAndView("redirect:/profile?deleteError=forbidden");
        }
        return new ModelAndView("redirect:/profile?deleted=1");
    }

    @RequestMapping(value = "/products/{id}/moderate-hide", method = RequestMethod.GET)
    public ModelAndView moderateHideFromReportMail(
        @PathVariable("id") final Long id,
        @RequestParam("token") final String token
    ) {
        if (!reportRemovalTokenService.isValid(id, token)) {
            throw new IllegalArgumentException("Invalid or expired moderation link");
        }
        productService.hideProductByAdmin(id);
        return redirectAfterModerationHide();
    }

    private static ModelAndView redirectAfterModerationHide() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return new ModelAndView("redirect:/?moderated=1");
        }
        return new ModelAndView("redirect:/login?moderated=1");
    }

    /** Not logged in → login; no CBU/CVU → profile Mis datos with warning. Empty if OK to show or submit the publish form. */
    private Optional<ModelAndView> redirectIfCannotPublish(final PawAuthUser authUser) {
        if (authUser == null) {
            return Optional.of(new ModelAndView("redirect:/login"));
        }
        final User publisher = userService.findById(authUser.getUser().getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        if (!publisher.hasCbuCvu() || !publisher.hasNeighborhoodAndProvince()) {
            return Optional.of(new ModelAndView("redirect:/profile?tab=mydata&missingData=publish"));
        }
        return Optional.empty();
    }

    private void attachProductFormSuggestions(final ModelAndView mav) {
        mav.addObject("artistSuggestions", productService.listDistinctArtists());
        mav.addObject("recordLabelSuggestions", productService.listDistinctRecordLabels());
    }

    private ModelAndView productFormView() {
        final ModelAndView mav = new ModelAndView("product-form");
        mav.addObject("isEditing", Boolean.FALSE);
        attachProductFormSuggestions(mav);
        return mav;
    }
}

