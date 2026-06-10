package ar.edu.itba.paw.webapp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.webapp.Util;
import ar.edu.itba.paw.webapp.form.ProductForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator.ValidatedImage;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductImageData;
import ar.edu.itba.paw.services.ProductImageUpdate;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.ReportService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.validation.ProductImageLayoutParser;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;

@Controller
public class ProductController {

    private static final int AUTOCOMPLETE_LIMIT = 7;
    private static final int AUTOCOMPLETE_MIN_QUERY_LENGTH = 2;
    private static final int PRODUCT_FORM_TEXT_MAX_LENGTH = 100;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final ReportService reportService;
    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public ProductController(
            final ProductService productService,
            final CategoryService categoryService,
            final ImageService imageService,
            final ReportService reportService,
            final ReviewService reviewService,
            final UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.reportService = reportService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.findAll();
    }

    @InitBinder("productForm")
    public void initProductFormBinder(
            final WebDataBinder binder,
            @PathVariable(value = "id", required = false) final Long productId) {
        binder.setDisallowedFields("editing", "productId", "hadExistingImages");
        final Object target = binder.getTarget();
        if (target instanceof ProductForm form) {
            populateProductFormContext(form, productId);
        }
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
        return redirectIfCannotPublishProducts(authUser).orElseGet(this::productFormView);
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ModelAndView createProduct(
            @AuthenticationPrincipal PawAuthUser authUser,
            @Valid @ModelAttribute("productForm") final ProductForm form,
            final BindingResult errors) {

        final Optional<ModelAndView> publishGuard = redirectIfCannotPublishProducts(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        if (errors.hasErrors()) {
            return productFormView();
        }

        final Product product = productService.createProduct(
                authUser.getUser().getId(),
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
                form.getStock(),
                imageDataFrom(form.getImages()));

        return new ModelAndView("redirect:/products/" + product.getId() + "?created=1");
    }

    @RequestMapping(value = "/products/{id:\\d+}/edit", method = RequestMethod.GET)
    public ModelAndView editProductForm(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @ModelAttribute("productForm") final ProductForm form) {
        final Optional<ModelAndView> publishGuard = redirectIfCannotPublishProducts(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        final Product product = productService.findEditableProduct(id, authUser.getUser().getId())
                .orElseThrow(ResourceNotFoundException::new);

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
        final Optional<ModelAndView> publishGuard = redirectIfCannotPublishProducts(authUser);
        if (publishGuard.isPresent()) {
            return publishGuard.get();
        }

        if (errors.hasErrors()) {
            return editProductFormModelAndView(id);
        }

        final ProductImageUpdate imageUpdate = buildImageUpdateFromForm(form);
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
                form.getStock(),
                imageUpdate);

        return new ModelAndView("redirect:/products/" + id + "?updated=1");
    }

    @RequestMapping(value = "/products/{id:\\d+}/restore", method = RequestMethod.POST)
    public ModelAndView restoreDeletedProduct(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id,
            final RedirectAttributes redirectAttributes) {
        if (!productService.restoreUserDeletedProduct(id, authUser.getUser().getId())) {
            redirectAttributes.addFlashAttribute("restoreError", true);
            return new ModelAndView("redirect:/profile?tab=trash");
        }
        redirectAttributes.addFlashAttribute("restored", true);
        return new ModelAndView("redirect:/profile?tab=trash");
    }

    private static List<ProductImageData> imageDataFrom(final MultipartFile[] files) {
        final List<ValidatedImage> validatedImages = ImageUploadValidator.readAll(files);
        final List<ProductImageData> images = new ArrayList<>(validatedImages.size());
        for (ValidatedImage image : validatedImages) {
            images.add(new ProductImageData(image.getData(), image.getContentType()));
        }
        return images;
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
            final boolean isOwnProduct = product.getUserId() == authUser.getUser().getId();
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

        final List<Product> relatedProducts = productService.getRelatedProducts(
                product,
                authUser != null ? authUser.getUser().getId() : null,
                10
        );

        final List<Product> carouselProducts = new ArrayList<>(sellerProducts);
        carouselProducts.addAll(relatedProducts);
        final Map<Long, SellerRatingSummary> sellerRatings = reviewService.sellerRatingByProducts(carouselProducts);

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
            @PathVariable("id") final Long id,
            @RequestParam(value = "back", required = false) final String back) {

        String queryParams = "";
        try {
            reportService.report(id, authUser.getUser().getId());
            queryParams = "reported=1";
        } catch (IllegalStateException e) {
            queryParams = "alreadyReported=1";
        } catch (IllegalArgumentException e) {
            queryParams = "reportError=1";
        }

        String redirectUrl = "redirect:/products/" + id + "?" + queryParams;
        if (back != null && !back.isBlank()) {
            redirectUrl += "&back=" + java.net.URLEncoder.encode(back, java.nio.charset.StandardCharsets.UTF_8);
        }

        return new ModelAndView(redirectUrl);
    }

    @RequestMapping(value = "/products/{id:\\d+}/delete", method = RequestMethod.POST)
    public ModelAndView deleteOwnProduct(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @PathVariable("id") final Long id,
            final RedirectAttributes redirectAttributes) {

        if (!productService.hideProductByUser(id, authUser.getUser().getId())) {
            redirectAttributes.addFlashAttribute("deleteError", "forbidden");
            return new ModelAndView("redirect:/profile?tab=publications");
        }
        redirectAttributes.addFlashAttribute("deleted", true);
        return new ModelAndView("redirect:/profile?tab=publications");
    }

    /**
     * No CBU/CVU → profile Mis datos with warning. Empty if OK to show or submit
     * the publish form.
     * Authentication is enforced by Spring Security.
     */
    private Optional<ModelAndView> redirectIfCannotPublishProducts(final PawAuthUser authUser) {
        if (!productService.canPublishProducts(authUser.getUser().getId())) {
            return Optional.of(new ModelAndView("redirect:/profile?tab=mydata&missingData=publish"));
        }
        return Optional.empty();
    }

    private void populateProductFormContext(final ProductForm form, final Long productId) {
        final boolean editing = productId != null;
        form.setEditing(editing);
        form.setProductId(productId);
        form.setHadExistingImages(editing && imageService.existsByProductId(productId));
    }

    private ModelAndView productFormView() {
        final ModelAndView mav = new ModelAndView("product-form");
        mav.addObject("isEditing", Boolean.FALSE);
        return mav;
    }

    private ProductImageUpdate buildImageUpdateFromForm(final ProductForm form) {
        final List<ProductImageData> newImages = imageDataFrom(form.getImages());
        if (!form.isHadExistingImages()) {
            if (newImages.isEmpty()) {
                return ProductImageUpdate.unchanged();
            }
            return ProductImageUpdate.replaceWithNewImages(newImages);
        }
        final String layout = form.getImageLayout();
        if (layout == null || layout.isBlank()) {
            return newImages.isEmpty() ? ProductImageUpdate.unchanged() : ProductImageUpdate.replaceWithNewImages(newImages);
        }
        final List<ProductImageLayoutParser.Slot> slots = ProductImageLayoutParser.parse(layout);
        final List<ProductImageUpdate.Entry> entries = new ArrayList<>(slots.size());
        int newImageIndex = 0;
        for (ProductImageLayoutParser.Slot slot : slots) {
            if (slot.getKind() == ProductImageLayoutParser.SlotKind.EXISTING) {
                entries.add(ProductImageUpdate.existingImage(slot.getExistingImageId()));
            } else {
                entries.add(ProductImageUpdate.newImage(newImages.get(newImageIndex++)));
            }
        }
        return ProductImageUpdate.replaceWith(entries);
    }
}
