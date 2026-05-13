package ar.edu.itba.paw.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
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
import javax.servlet.http.HttpServletRequest;

import ar.edu.itba.paw.models.Category;
import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.webapp.form.ProductForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser.Slot;
import ar.edu.itba.paw.webapp.product.ProductImageLayoutParser.SlotKind;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator;
import ar.edu.itba.paw.webapp.validation.ImageUploadValidator.InvalidImageUploadException;
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
        final UserService userService
    ) {
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

    @RequestMapping(value = "/products/{id:\\d+}/edit", method = RequestMethod.GET)
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

        return editProductFormModelAndView(id);
    }

    @RequestMapping(value = "/products/{id:\\d+}/edit", method = RequestMethod.POST)
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
            return editProductFormModelAndView(id);
        }

        final boolean hadImages = imageService.existsByProductId(id);
        final String layoutRaw = form.getImageLayout();
        final boolean useLayout = hadImages && layoutRaw != null && !layoutRaw.isBlank();

        final List<ValidatedImage> replacementImages;

        if (!hadImages) {
            final List<ValidatedImage> validatedImages;
            try {
                validatedImages = ImageUploadValidator.validateAll(form.getImages());
            } catch (InvalidImageUploadException e) {
                errors.rejectValue("images", "Invalid.productForm.images", e.getMessage());
                return editProductFormModelAndView(id);
            } catch (IOException e) {
                errors.rejectValue("images", "Read.productForm.images", null);
                return editProductFormModelAndView(id);
            }
            if (validatedImages.isEmpty()) {
                errors.rejectValue("images", "Required.productForm.images", null);
                return editProductFormModelAndView(id);
            }
            replacementImages = validatedImages;
        } else if (useLayout) {
            final List<Slot> slots;
            try {
                slots = ProductImageLayoutParser.parse(layoutRaw);
            } catch (RuntimeException ex) {
                errors.rejectValue("images", "Invalid.productForm.imageLayout", null);
                return editProductFormModelAndView(id);
            }
            if (slots.isEmpty() || slots.size() > ImageUploadValidator.MAX_IMAGES_PER_PRODUCT) {
                errors.rejectValue("images", "Invalid.productForm.imageLayout", null);
                return editProductFormModelAndView(id);
            }
            final long newSlotCount = slots.stream().filter(s -> s.getKind() == SlotKind.NEW).count();
            final List<org.springframework.web.multipart.MultipartFile> newFiles =
                extractNonEmptyMultipartFilesList(form.getImages());
            if (newSlotCount != newFiles.size()) {
                errors.rejectValue("images", "Invalid.productForm.imageLayout", null);
                return editProductFormModelAndView(id);
            }
            long newBytesTotal = 0;
            for (org.springframework.web.multipart.MultipartFile f : newFiles) {
                newBytesTotal += f.getSize();
            }
            if (newBytesTotal > ImageUploadValidator.MAX_REQUEST_BYTES) {
                errors.rejectValue("images", "Invalid.productForm.images", null);
                return editProductFormModelAndView(id);
            }
            final List<ValidatedImage> built = new ArrayList<>(slots.size());
            int newFileIndex = 0;
            for (final Slot slot : slots) {
                if (slot.getKind() == SlotKind.EXISTING) {
                    final Optional<Image> imgOpt = imageService.findById(slot.getExistingImageId());
                    if (imgOpt.isEmpty() || !imgOpt.get().getProductId().equals(id)) {
                        errors.rejectValue("images", "Invalid.productForm.imageLayout", null);
                        return editProductFormModelAndView(id);
                    }
                    try {
                        built.add(ImageUploadValidator.validateStoredImageBytes(imgOpt.get().getData()));
                    } catch (InvalidImageUploadException e) {
                        errors.rejectValue("images", "Invalid.productForm.images", e.getMessage());
                        return editProductFormModelAndView(id);
                    } catch (IOException e) {
                        errors.rejectValue("images", "Read.productForm.images", null);
                        return editProductFormModelAndView(id);
                    }
                } else {
                    try {
                        built.add(ImageUploadValidator.validate(newFiles.get(newFileIndex++)));
                    } catch (InvalidImageUploadException e) {
                        errors.rejectValue("images", "Invalid.productForm.images", e.getMessage());
                        return editProductFormModelAndView(id);
                    } catch (IOException e) {
                        errors.rejectValue("images", "Read.productForm.images", null);
                        return editProductFormModelAndView(id);
                    }
                }
            }
            replacementImages = built;
        } else {
            final boolean hasNewImages = hasNonEmptyMultipartFiles(form.getImages());
            if (hasNewImages) {
                final List<ValidatedImage> validatedImages = new ArrayList<>();
                try {
                    validatedImages.addAll(ImageUploadValidator.validateAll(form.getImages()));
                } catch (InvalidImageUploadException e) {
                    errors.rejectValue("images", "Invalid.productForm.images", e.getMessage());
                    return editProductFormModelAndView(id);
                } catch (IOException e) {
                    errors.rejectValue("images", "Read.productForm.images", null);
                    return editProductFormModelAndView(id);
                }
                if (validatedImages.isEmpty()) {
                    errors.rejectValue("images", "Required.productForm.images", null);
                    return editProductFormModelAndView(id);
                }
                replacementImages = validatedImages;
            } else {
                replacementImages = null;
            }
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
        @PathVariable("id") final Long id
    ) {
        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }
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
        attachProductFormSuggestions(mav);
        return mav;
    }

    @RequestMapping(value = "/products/{id:\\d+}", method = RequestMethod.GET)
    public ModelAndView productDetail(
        @PathVariable("id") final Long id,
        @AuthenticationPrincipal final PawAuthUser authUser,
        final HttpServletRequest request,
        @ModelAttribute("purchaseCreateForm") final ar.edu.itba.paw.webapp.form.PurchaseCreateForm purchaseForm
    ) {
        final Product product = productService.findByIdIfAvailable(id)
            .orElseThrow(ResourceNotFoundException::new);

        final ModelAndView mav = new ModelAndView("product-detail");
        mav.addObject("product", product);
        mav.addObject("productDetailBackUrl", resolveProductDetailBackUrl(request, id));

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

        List<Product> sellerProducts = productService.listProductsByUserExcept(product.getUserId(), product.getId());

        List<Product> relatedProducts = productService.listProductsByArtistExcept(product.getArtist(), product.getId());

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
        final Map<Long, SellerRatingSummary> sellerRatings = new HashMap<>();
        for (Long uid : carouselSellerIds) {
            sellerRatings.put(uid, reviewService.summaryForSeller(uid));
        }

        mav.addObject("sellerProducts", sellerProducts);
        mav.addObject("relatedProducts", relatedProducts);
        mav.addObject("sellerRatings", sellerRatings);

        return mav;
    }

    static String resolveProductDetailBackUrl(final HttpServletRequest request, final Long productId) {
        final String fallbackUrl = "/";
        final String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return fallbackUrl;
        }

        try {
            final URI refererUri = URI.create(referer);
            if ((refererUri.isAbsolute() || refererUri.getHost() != null)
                && !isSameOrigin(refererUri, request)) {
                return fallbackUrl;
            }

            final String backPath = internalPathFromReferer(refererUri.getRawPath(), request.getContextPath());
            if (backPath == null || isSameProductDetailPath(backPath, productId)) {
                return fallbackUrl;
            }

            final String query = refererUri.getRawQuery();
            return query == null || query.isBlank() ? backPath : backPath + "?" + query;
        } catch (IllegalArgumentException e) {
            return fallbackUrl;
        }
    }

    private static boolean isSameOrigin(final URI refererUri, final HttpServletRequest request) {
        final String refererHost = refererUri.getHost();
        if (refererHost == null || !refererHost.equalsIgnoreCase(request.getServerName())) {
            return false;
        }
        if (refererUri.getScheme() != null && !refererUri.getScheme().equalsIgnoreCase(request.getScheme())) {
            return false;
        }
        return effectivePort(refererUri.getScheme(), refererUri.getPort())
            == effectivePort(request.getScheme(), request.getServerPort());
    }

    private static int effectivePort(final String scheme, final int port) {
        if (port > 0) {
            return port;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        return 80;
    }

    private static String internalPathFromReferer(final String rawPath, final String contextPath) {
        String path = rawPath == null || rawPath.isBlank() ? "/" : rawPath;
        if (contextPath != null && !contextPath.isBlank()) {
            if (path.equals(contextPath)) {
                return "/";
            }
            if (!path.startsWith(contextPath + "/")) {
                return null;
            }
            path = path.substring(contextPath.length());
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static boolean isSameProductDetailPath(final String path, final Long productId) {
        return path.equals("/products/" + productId);
    }

    @RequestMapping(value = "/products/{id:\\d+}/report", method = RequestMethod.POST)
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

        reportService.report(id, authUser.getUser().getId(), product.getUserId());

        return new ModelAndView("redirect:/products/" + id + "?reported=1");
    }

    @RequestMapping(value = "/products/{id:\\d+}/delete", method = RequestMethod.POST)
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

