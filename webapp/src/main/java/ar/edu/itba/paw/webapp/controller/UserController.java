package ar.edu.itba.paw.webapp.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.PurchaseService;
import ar.edu.itba.paw.services.VerificationTokenService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.ReportService;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.LoginForm;
import ar.edu.itba.paw.webapp.form.UserProfileForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.Util;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.ProductSearchCriteria;

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private static final int PROFILE_PUBLICATIONS_PAGE_SIZE = 12;
    private static final int PROFILE_OTHER_PAGE_SIZE = 3;
    private static final int PROFILE_TRASH_PAGE_SIZE = 12;
    private static final int PROFILE_FOLLOW_PAGE_SIZE = 12;
    private static final int PROFILE_WISHLIST_LIMIT = 9;

    private final UserService userService;
    private final ProductService productService;
    private final ImageService imageService;
    private final PurchaseService purchaseService;
    private final ReviewService reviewService;
    private final ReportService reportService;
    private final VerificationTokenService verificationTokenService;
    private final CategoryService categoryService;

    @Autowired
    public UserController(
            final UserService userService,
            final ProductService productService,
            final ImageService imageService,
            final PurchaseService purchaseService,
            final ReviewService reviewService,
            final ReportService reportService,
            final VerificationTokenService verificationTokenService,
            final CategoryService categoryService) {

        this.userService = userService;
        this.productService = productService;
        this.imageService = imageService;
        this.purchaseService = purchaseService;
        this.reviewService = reviewService;
        this.reportService = reportService;
        this.verificationTokenService = verificationTokenService;
        this.categoryService = categoryService;
    }

    @RequestMapping(value = "/login")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("loginForm", new LoginForm());
        return mv;
    }

    @RequestMapping(value = "/register")
    public ModelAndView register(@ModelAttribute RegisterForm form) {
        ModelAndView mv = new ModelAndView("register");
        mv.addObject("registerForm", form);
        return mv;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView createUser(
            @Valid @ModelAttribute RegisterForm form,
            final BindingResult errors) {

        ModelAndView mv = new ModelAndView("register");
        mv.addObject("registerForm", form);

        if (errors.hasErrors()) {
            return mv;
        }

        LOGGER.atDebug().addArgument(form.getEmail()).log("About to attempt register email {}");

        final java.util.Optional<User> user = userService.createUserIfEmailAvailable(
                    form.getEmail(),
                    form.getPassword(),
                    form.getUsername(),
                    false,
                    false,
                    form.getFirstName(),
                    form.getLastName(),
                    form.getStreetName(),
                    form.getStreetNumber(),
                    form.getNeighborhood(),
                    form.getProvince(),
                    form.getExtraAddressInfo(),
                    form.getCbuCvu());
        if (!user.isPresent()) {
            errors.rejectValue("email", "EmailInUse.authForm.email");
            return mv;
        }

        verificationTokenService.createVerificationTokenForUser(user.get().getId());
        return new ModelAndView("redirect:/sendVerificationEmail");
    }

    @RequestMapping(value = "/profile")
    public ModelAndView profile(
            @AuthenticationPrincipal PawAuthUser authUser,
            @RequestParam(value = "userId", required = false) final Long userId,
            @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "trashPage", defaultValue = "1") final int trashPage,
            @RequestParam(value = "status", required = false) final List<PurchaseStatus> statuses,
            final Model model) {
        if (page < 1) {
            throw new IllegalArgumentException("Invalid page");
        }
        if (trashPage < 1) {
            throw new IllegalArgumentException("Invalid trash page");
        }

        final boolean isOwnProfile;
        final User profileUser;

        if (userId != null) {
            profileUser = userService.findById(userId)
                    .orElseThrow(ResourceNotFoundException::new);
            if (!profileUser.getEnabled()) {
                throw new ResourceNotFoundException();
            }
            isOwnProfile = (authUser != null && authUser.getUser().getId().equals(userId));
        } else {
            profileUser = userService.findById(authUser.getUser().getId())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            isOwnProfile = true;
        }

        List<PurchaseStatus> purchaseStatuses = new ArrayList<>();
        List<String> validStatusesStr = new ArrayList<>();
        if (statuses != null) {
            for (PurchaseStatus status : statuses) {
                if (status != null) {
                    purchaseStatuses.add(status);
                    validStatusesStr.add(status.name());
                }
            }
        }

        final ModelAndView mv = new ModelAndView("profile");
        enrichProfileModel(mv, profileUser, isOwnProfile, authUser, page, trashPage, purchaseStatuses);
        mv.addObject("selectedStatuses", validStatusesStr);

        if (isOwnProfile && !model.containsAttribute("userProfileForm")) {
            mv.addObject("userProfileForm", UserProfileForm.fromUser(profileUser));
        }
        
        return mv;
    }

    @RequestMapping(value = "/profile/update", method = RequestMethod.POST)
    public ModelAndView updateProfile(
            @AuthenticationPrincipal PawAuthUser authUser,
            @Valid @ModelAttribute("userProfileForm") final UserProfileForm form,
            final BindingResult errors,
            final RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    BindingResult.MODEL_KEY_PREFIX + "userProfileForm", errors);
            redirectAttributes.addFlashAttribute("userProfileForm", form);
            return new ModelAndView("redirect:/profile?tab=mydata");
        }

        final User profileUser = userService.findById(authUser.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        userService.updateUserProfile(
                profileUser.getId(),
                form.getFirstName(),
                form.getLastName(),
                form.getStreetName(),
                form.getStreetNumber(),
                form.getNeighborhood(),
                form.getProvince(),
                form.getExtraAddressInfo(),
                form.getCbuCvu(),
                form.getLanguage());

        final User refreshed = userService.findById(profileUser.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Util.refreshAuthenticationPrincipal(authUser, refreshed);

        return new ModelAndView("redirect:/profile?tab=mydata&updated=1");
    }

    @RequestMapping(value = "/profile/update-genres", method = RequestMethod.POST)
    public ModelAndView updateGenres(
            @AuthenticationPrincipal PawAuthUser authUser,
            @RequestParam(value = "favoriteCategories", required = false) final List<Long> categoryIds) {

        final User profileUser = userService.findById(authUser.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        userService.updateFavoriteCategories(profileUser.getId(), categoryIds != null ? categoryIds : Collections.emptyList());

        return new ModelAndView("redirect:/profile?tab=mydata&updated=1");
    }

    private void enrichProfileModel(
            final ModelAndView mv,
            final User profileUser,
            final boolean isOwnProfile,
            final PawAuthUser authUser,
            final int page,
            final int trashPage,
            final List<PurchaseStatus> statuses) {
        final ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                profileUser.getId(),
                page,
                PROFILE_PUBLICATIONS_PAGE_SIZE);

        final PaginatedResult<Product> productsPage = productService.listProducts(criteria);

        final Map<Long, String> productImageUrls = productImageUrlsFor(productsPage.getResults());

        mv.addObject("user", profileUser);
        mv.addObject("isOwnProfile", isOwnProfile);
        mv.addObject("userProductsPage", productsPage);
        mv.addObject("userProducts", productsPage.getResults());
        mv.addObject("productImageUrls", productImageUrls);

        if (isOwnProfile) {
            mv.addObject("allCategories", categoryService.findAll());
            mv.addObject("userFavoriteCategoryIds", profileUser.getFavoriteCategories().stream()
                .map(ar.edu.itba.paw.models.Category::getId)
                .collect(java.util.stream.Collectors.toList()));
        }

        final List<Product> wishlistProducts = userService.getWishlistProducts(profileUser.getId(), PROFILE_WISHLIST_LIMIT);
        final Map<Long, String> wishlistProductImageUrls = productImageUrlsFor(wishlistProducts);

        mv.addObject("wishlistProducts", wishlistProducts);
        mv.addObject("wishlistProductImageUrls", wishlistProductImageUrls);

        mv.addObject("followerCount", userService.countFollowers(profileUser.getId()));
        mv.addObject("followingCount", userService.countFollowing(profileUser.getId()));

        if (!isOwnProfile && authUser != null) {
            mv.addObject("isFollowing", userService.isFollowing(authUser.getUser().getId(), profileUser.getId()));
        }

        PaginatedResult<ar.edu.itba.paw.models.Review> reviewsPage = reviewService.findBySellerId(profileUser.getId(),
                page, PROFILE_OTHER_PAGE_SIZE);
        mv.addObject("receivedReviewsPage", reviewsPage);
        mv.addObject("receivedReviews", reviewsPage.getResults());
        mv.addObject("sellerRating", reviewService.summaryForSeller(profileUser.getId()));

        if (isOwnProfile && authUser != null) {
            final PaginatedResult<Purchase> purchasesPage = purchaseService.findByBuyerId(profileUser.getId(), statuses, page,
                    PROFILE_OTHER_PAGE_SIZE);

            final Map<Long, Product> purchaseProducts = productsByPurchaseId(purchasesPage.getResults());
            final Map<Long, Boolean> purchaseHasReview = reviewStatusByPurchaseId(purchasesPage.getResults());

            mv.addObject("purchasesPage", purchasesPage);
            mv.addObject("purchases", purchasesPage.getResults());
            mv.addObject("purchaseProducts", purchaseProducts);
            mv.addObject("purchaseHasReview", purchaseHasReview);

            final PaginatedResult<Purchase> salesPage = purchaseService.findBySellerId(profileUser.getId(), statuses, page,
                    PROFILE_OTHER_PAGE_SIZE);

            final Map<Long, Product> saleProducts = productsByPurchaseId(salesPage.getResults());

            mv.addObject("salesPage", salesPage);
            mv.addObject("sales", salesPage.getResults());
            mv.addObject("saleProducts", saleProducts);

            reportService.findAllGroupedByProductForAdmin(authUser.getUser().getId(), page, PROFILE_OTHER_PAGE_SIZE)
                    .ifPresent(reportsPage -> {
                        mv.addObject("reportsPage", reportsPage);
                        mv.addObject("reportedProducts", reportsPage.getResults());
                    });

            final PaginatedResult<Product> deletedPage = productService.listUserDeletedProducts(profileUser.getId(),
                    trashPage, PROFILE_TRASH_PAGE_SIZE);
            final Map<Long, String> deletedProductImageUrls = productImageUrlsFor(deletedPage.getResults());

            LOGGER.atDebug().addArgument(deletedPage.getResults()).log("Found the following deleted products: {}");

            mv.addObject("deletedProductsPage", deletedPage);
            mv.addObject("deletedProducts", deletedPage.getResults());
            mv.addObject("deletedProductImageUrls", deletedProductImageUrls);
        }

        final PaginatedResult<User> followersPage = userService.getFollowers(profileUser.getId(), page, PROFILE_FOLLOW_PAGE_SIZE);
        mv.addObject("followersPage", followersPage);
        mv.addObject("followers", followersPage.getResults());

        final PaginatedResult<User> followingPage = userService.getFollowing(profileUser.getId(), page, PROFILE_FOLLOW_PAGE_SIZE);
        mv.addObject("followingPage", followingPage);
        mv.addObject("followingUsers", followingPage.getResults());

        if (authUser != null) {
            mv.addObject("followStatusMap", followStatusMapFor(
                    authUser.getUser().getId(),
                    followersPage.getResults(),
                    followingPage.getResults()));
        }
    }

    private Map<Long, String> productImageUrlsFor(final List<Product> products) {
        final Map<Long, String> productImageUrls = new HashMap<>();
        if (products == null || products.isEmpty()) {
            return productImageUrls;
        }

        final List<Long> productIds = new ArrayList<>();
        for (Product product : products) {
            productIds.add(product.getId());
        }
        final Set<Long> productIdsWithImages = imageService.findProductIdsWithImages(productIds);
        for (Long productId : productIdsWithImages) {
            productImageUrls.put(productId, "/images/product/" + productId);
        }
        return productImageUrls;
    }

    private Map<Long, Product> productsByPurchaseId(final List<Purchase> purchases) {
        final Map<Long, Product> productsByPurchaseId = new HashMap<>();
        if (purchases == null || purchases.isEmpty()) {
            return productsByPurchaseId;
        }

        final Set<Long> productIds = new HashSet<>();
        for (Purchase purchase : purchases) {
            productIds.add(purchase.getProductId());
        }

        final Map<Long, Product> productsById = new HashMap<>();
        for (Product product : productService.findByIds(productIds)) {
            productsById.put(product.getId(), product);
        }

        for (Purchase purchase : purchases) {
            final Product product = productsById.get(purchase.getProductId());
            if (product != null) {
                productsByPurchaseId.put(purchase.getPurchaseId(), product);
            }
        }
        return productsByPurchaseId;
    }

    private Map<Long, Boolean> reviewStatusByPurchaseId(final List<Purchase> purchases) {
        final Map<Long, Boolean> purchaseHasReview = new HashMap<>();
        if (purchases == null || purchases.isEmpty()) {
            return purchaseHasReview;
        }

        final Set<Long> purchaseIds = new HashSet<>();
        for (Purchase purchase : purchases) {
            purchaseIds.add(purchase.getPurchaseId());
        }
        final Set<Long> reviewedPurchaseIds = reviewService.findReviewedPurchaseIds(purchaseIds);
        for (Purchase purchase : purchases) {
            purchaseHasReview.put(purchase.getPurchaseId(), reviewedPurchaseIds.contains(purchase.getPurchaseId()));
        }
        return purchaseHasReview;
    }

    private Map<Long, Boolean> followStatusMapFor(
            final Long currentUserId,
            final List<User> followers,
            final List<User> following) {
        final List<Long> userIds = new ArrayList<>();
        final Set<Long> seenIds = new HashSet<>();
        if (followers != null) {
            for (User user : followers) {
                if (seenIds.add(user.getId())) {
                    userIds.add(user.getId());
                }
            }
        }
        if (following != null) {
            for (User user : following) {
                if (seenIds.add(user.getId())) {
                    userIds.add(user.getId());
                }
            }
        }
        return userService.followingStatusByUserIds(currentUserId, userIds);
    }

    @RequestMapping(value = "/profile/admin/hide-product", method = RequestMethod.POST)
    public ModelAndView adminHideProduct(
            @RequestParam("productId") final Long productId) {
        // Authorization enforced by Spring Security: only ROLE_ADMIN can reach this
        // endpoint
        productService.hideProductByAdmin(productId);
        return new ModelAndView("redirect:/profile?tab=reports&hidden=1");
    }

    @RequestMapping(value = "/profile/admin/ban-user", method = RequestMethod.POST)
    public ModelAndView adminBanUser(
            @RequestParam("userId") final Long userId) {
        // Ban the user
        userService.ban(userId);

        return new ModelAndView("redirect:/profile?tab=reports&banned=1");
    }

    @RequestMapping(value = "/profile/trash", method = RequestMethod.GET)
    public ModelAndView trash(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @RequestParam(value = "page", defaultValue = "1") final int page) {
        if (page < 1) {
            throw new IllegalArgumentException("Invalid page");
        }
        return new ModelAndView("redirect:/profile?tab=trash&trashPage=" + page);
    }

    @RequestMapping(value = "/toggle-wishlist-product", method = RequestMethod.POST)
    public ModelAndView addWishlistProduct(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @RequestParam("productId") final Long productId,
            HttpServletRequest request) {

        userService.toggleWishlistProduct(authUser.getUser().getId(), productId);

        String referer = request.getHeader("Referer");

        return new ModelAndView("redirect:" + referer);
    }

    @RequestMapping(value = "/profile/follow", method = RequestMethod.POST)
    public ModelAndView toggleFollow(
            @AuthenticationPrincipal final PawAuthUser authUser,
            @RequestParam("userId") final Long targetUserId,
            HttpServletRequest request) {

        final Long currentUserId = authUser.getUser().getId();

        userService.toggleFollow(currentUserId, targetUserId);

        final String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return new ModelAndView("redirect:" + referer);
        }
        return new ModelAndView("redirect:/profile?userId=" + targetUserId);
    }

}
