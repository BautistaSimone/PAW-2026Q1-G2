package ar.edu.itba.paw.webapp.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.webapp.Util;
import ar.edu.itba.paw.webapp.form.ProductForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser.Slot;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser.SlotKind;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator.ValidatedImage;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.ReportService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;

@Controller
public class ProductController {

    private static final int AUTOCOMPLETE_LIMIT = 7;
    private static final int AUTOCOMPLETE_MIN_QUERY_LENGTH = 2;
    private static final int PRODUCT_FORM_TEXT_MAX_LENGTH = 100;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final EmailService emailService;
    private final ReportService reportService;
    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ProductController(
            final ProductService productService,
            final CategoryService categoryService,
            final ImageService imageService,
            final EmailService emailService,
            final ReportService reportService,
            final ReviewService reviewService,
            final UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.emailService = emailService;
        this.reportService = reportService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.findAll();
    }

    @ResponseBody
    @RequestMapping(value = "/products/autocomplete/artists", method = RequestMethod.GET, produces = "application/json")
    public List<String> artistAutocomplete(@RequestParam(value = "q", required = false) final String query) {
        final String normalizedQuery = normalizeAutocompleteQuery(query);
        if (normalizedQuery.length() < AUTOCOMPLETE_MIN_QUERY_LENGTH) {
            return List.of();
        }
        return productService.suggestArtists(normalizedQuery, AUTOCOMPLETE_LIMIT);
    }

    @ResponseBody
    @RequestMapping(value = "/products/autocomplete/record-labels", method = RequestMethod.GET, produces = "application/json")
    public List<String> recordLabelAutocomplete(@RequestParam(value = "q", required = false) final String query) {
        final String normalizedQuery = normalizeAutocompleteQuery(query);
        if (normalizedQuery.length() < AUTOCOMPLETE_MIN_QUERY_LENGTH) {
            return List.of();
        }
        return productService.suggestRecordLabels(normalizedQuery, AUTOCOMPLETE_LIMIT);
    }

    private static String normalizeAutocompleteQuery(final String rawQuery) {
        final String trimmed = rawQuery == null ? "" : rawQuery.trim();
        if (trimmed.length() <= PRODUCT_FORM_TEXT_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, PRODUCT_FORM_TEXT_MAX_LENGTH);
    }

    @RequestMapping(value = "/products/new", method = RequestMethod.GET)
    public ModelAndView newProductForm(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @ModelAttribute("productForm") final ProductForm form) {
        return redirectIfMissingProfileData(authUser).orElseGet(this::productFormView);
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ModelAndView createProduct(
            @AuthenticationPrincipal PawAuthUser authUser,
            @Valid @ModelAttribute("productForm") final ProductForm form,
            final BindingResult errors) {

        final Optional<ModelAndView> publishGuard = redirectIfMissingProfileData(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        if (errors.hasErrors()) {
            return productFormView();
        }

        // Image validation for creation: at least one image is required
        final List<MultipartFile> presentFiles = extractNonEmptyMultipartFilesList(form.getImages());
        if (presentFiles.isEmpty()) {
            errors.rejectValue("images", "Required.productForm.images");
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
                form.getPrice(),
                form.getStock());

        // Images were validated by ProductFormValidator — read and persist directly.
        final List<ValidatedImage> validatedImages = ImageUploadValidator.readAll(form.getImages());
        for (ValidatedImage image : validatedImages) {
            imageService.createImage(
                    product.getId(),
                    image.getData(),
                    image.getContentType());
        }

        return new ModelAndView("redirect:/products/" + product.getId() + "?created=1");
    }

    @RequestMapping(value = "/products/{id:\\d+}/edit", method = RequestMethod.GET)
    public ModelAndView editProductForm(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @ModelAttribute("productForm") final ProductForm form) {
        final Optional<ModelAndView> publishGuard = redirectIfMissingProfileData(authUser);
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
                product.getCategories().stream().map(c -> c.getId()).collect(Collectors.toList()));
        form.setStock(product.getStock());

        return editProductFormModelAndView(id);
    }

    @RequestMapping(value = "/products/{id:\\d+}/edit", method = RequestMethod.POST)
    public ModelAndView updateProduct(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @Valid @ModelAttribute("productForm") final ProductForm form,
            final BindingResult errors) {
        final Optional<ModelAndView> publishGuard = redirectIfMissingProfileData(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        final Product existing = productService.findByIdIfAvailable(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (!existing.getUserId().equals(authUser.getUser().getId())) {
            throw new ResourceNotFoundException();
        }

        if (errors.hasErrors()) {
            return editProductFormModelAndView(id);
        }

        final boolean hadImages = imageService.existsByProductId(id);
        final String layoutRaw = form.getImageLayout();
        final boolean useLayout = hadImages && layoutRaw != null && !layoutRaw.isBlank();

        // Image handling for edit: if no existing images, new ones are required
        final List<ValidatedImage> replacementImages;
        if (!hadImages) {
            final List<MultipartFile> presentFiles = extractNonEmptyMultipartFilesList(form.getImages());
            if (presentFiles.isEmpty()) {
                errors.rejectValue("images", "Required.productForm.images");
                return editProductFormModelAndView(id);
            }
            replacementImages = ImageUploadValidator.readAll(form.getImages());
        } else if (useLayout) {
            final List<Slot> slots = ProductImageLayoutParser.parse(layoutRaw);
            final List<org.springframework.web.multipart.MultipartFile> newFiles = extractNonEmptyMultipartFilesList(
                    form.getImages());
            final List<ValidatedImage> built = new ArrayList<>(slots.size());
            int newFileIndex = 0;
            for (final Slot slot : slots) {
                if (slot.getKind() == SlotKind.EXISTING) {
                    final Image img = imageService.findById(slot.getExistingImageId()).orElseThrow();
                    built.add(ImageUploadValidator.readStoredImageBytes(img.getData()));
                } else {
                    built.add(ImageUploadValidator.read(newFiles.get(newFileIndex++)));
                }
            }
            replacementImages = built;
        } else {
            replacementImages = hasNonEmptyMultipartFiles(form.getImages())
                    ? ImageUploadValidator.readAll(form.getImages())
                    : null;
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
                form.getPrice(),
                form.getStock());

        if (replacementImages != null) {
            imageService.deleteImagesByProductId(id);
            for (ValidatedImage image : replacementImages) {
                imageService.createImage(id, image.getData(), image.getContentType());
            }
        }

        return new ModelAndView("redirect:/products/" + id + "?updated=1");
    }

    @RequestMapping(value = "/products/{id:\\d+}/restore", method = RequestMethod.POST)
    public ModelAndView restoreDeletedProduct(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id) {
        if (!productService.restoreUserDeletedProduct(id, authUser.getUser().getId())) {
            return new ModelAndView("redirect:/profile?tab=trash&restoreError=1");
        }
        return new ModelAndView("redirect:/profile?tab=trash&restored=1");
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

    private static List<MultipartFile> extractNonEmptyMultipartFilesList(final MultipartFile[] files) {
        if (files == null) {
            return List.of();
        }
        final List<MultipartFile> out = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                out.add(f);
            }
        }
        return out;
    }

    private ModelAndView editProductFormModelAndView(final Long productId) {
        final ModelAndView mav = new ModelAndView("product-form");
        mav.addObject("isEditing", Boolean.TRUE);
        mav.addObject("editingProductId", productId);
        final boolean hasImg = imageService.existsByProductId(productId);
        mav.addObject("hasExistingProductImages", hasImg);
        if (hasImg) {
            final List<Long> ids = imageService.findAllByProductId(productId).stream()
                    .map(Image::getImageId)
                    .collect(Collectors.toList());
            mav.addObject("existingProductImageIds", ids);
        }
        return mav;
    }

    @RequestMapping(value = "/products/{id:\\d+}", method = RequestMethod.GET)
    public ModelAndView productDetail(
            @PathVariable("id") final Long id,
            @AuthenticationPrincipal final PawAuthUser authUser,
            final HttpServletRequest request,
            @ModelAttribute("purchaseCreateForm") final ar.edu.itba.paw.webapp.form.PurchaseCreateForm purchaseForm) {
        final Product product = productService.findByIdIfAvailable(id)
                .orElseThrow(ResourceNotFoundException::new);

        final ModelAndView mav = new ModelAndView("product-detail");
        mav.addObject("product", product);

        String backUrl = Util.resolveBackUrl(request);
        if (!isSameProductDetailPath(backUrl, id)) {
            mav.addObject("productDetailBackUrl", backUrl);
        }

        if (authUser != null) {
            final boolean isOwnProduct = product.getUserId().equals(authUser.getUser().getId());
            final boolean isWishlisted = userService.isProductInWishlist(authUser.getUser().getId(), product.getId());

            mav.addObject("isOwnProduct", isOwnProduct);
            mav.addObject("isWishlisted", isWishlisted);

        } else {
            mav.addObject("isOwnProduct", false);
            mav.addObject("isWishlisted", false);
        }

        final List<ar.edu.itba.paw.models.Image> productImages = imageService.findAllByProductId(product.getId());
        if (!productImages.isEmpty()) {
            mav.addObject("productImages", productImages);
            mav.addObject("productImageUrl", "/images/" + productImages.get(0).getImageId());
        }

        mav.addObject("sellerRating", reviewService.summaryForSeller(product.getUserId()));
        userService.findById(product.getUserId()).ifPresent(seller -> mav.addObject("seller", seller));
        mav.addObject("sellerReviews", reviewService.findBySellerId(product.getUserId(), 1, 3).getResults());

        List<Product> sellerProducts = productService.listProductsByUserExcept(product.getUserId(), product.getId());

        List<Product> relatedProducts = new ArrayList<>();
        if (authUser != null) {
            relatedProducts = productService.getRecommendedProducts(authUser.getUser().getId(), 10, product.getId());
        }

        if (relatedProducts.isEmpty()) {
            relatedProducts = productService.listProductsByArtistExcept(product.getArtist(), product.getId());
        }

        if (relatedProducts.isEmpty()) {
            relatedProducts = productService.listProductsNotByUser(product.getUserId());
        }

        final Set<Long> carouselSellerIds = new HashSet<>();
        for (Product p : sellerProducts) {
            carouselSellerIds.add(p.getUserId());
        }
        for (Product p : relatedProducts) {
            carouselSellerIds.add(p.getUserId());
        }
        final Map<Long, SellerRatingSummary> sellerRatings = reviewService.sellerRatingByUserId(carouselSellerIds);

        mav.addObject("sellerProducts", sellerProducts);
        mav.addObject("relatedProducts", relatedProducts);
        mav.addObject("sellerRatings", sellerRatings);

        return mav;
    }

    private static boolean isSameProductDetailPath(final String path, final Long productId) {
        return path.equals("/products/" + productId);
    }

    @RequestMapping(value = "/products/{id:\\d+}/report", method = RequestMethod.POST)
    public ModelAndView reportProduct(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id) {

        final Product product = productService.findByIdIfAvailable(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (reportService.hasReported(id, authUser.getUser().getId())) {
            return new ModelAndView("redirect:/products/" + id + "?alreadyReported=1");
        }

        reportService.report(id, authUser.getUser().getId(), product.getUserId());

        return new ModelAndView("redirect:/products/" + id + "?reported=1");
    }

    @RequestMapping(value = "/products/{id:\\d+}/delete", method = RequestMethod.POST)
    public ModelAndView deleteOwnProduct(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id) {

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

    /**
     * No CBU/CVU → profile Mis datos with warning. Empty if OK to show or submit the publish form.
     * Authentication is enforced by Spring Security.
     */
    private Optional<ModelAndView> redirectIfMissingProfileData(final PawAuthUser authUser) {
        final User publisher = userService.findById(authUser.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (!publisher.hasCbuCvu() || !publisher.hasNeighborhoodAndProvince()) {
            return Optional.of(new ModelAndView("redirect:/profile?tab=mydata&missingData=publish"));
        }
        return Optional.empty();
    }

    private ModelAndView productFormView() {
        final ModelAndView mav = new ModelAndView("product-form");
        mav.addObject("isEditing", Boolean.FALSE);
        return mav;
    }
}
