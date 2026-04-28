package ar.edu.itba.paw.webapp.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.LoginForm;
import ar.edu.itba.paw.webapp.form.UserProfileForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.ProductSearchCriteria;

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ProductService productService;
    private final ImageService imageService;
    private final PurchaseService purchaseService;
    private final ReviewService reviewService;
    private final ReportService reportService;
    private final VerificationTokenService verificationTokenService;

    @Autowired
    public UserController(
        final UserService userService,
        final ProductService productService,
        final ImageService imageService,
        final PurchaseService purchaseService,
        final ReviewService reviewService,
        final ReportService reportService,
        final VerificationTokenService verificationTokenService) {

        this.userService = userService;
        this.productService = productService;
        this.imageService = imageService;
        this.purchaseService = purchaseService;
        this.reviewService = reviewService;
        this.reportService = reportService;
        this.verificationTokenService = verificationTokenService;
    }

    @RequestMapping(value = "/login")
    public ModelAndView login(@AuthenticationPrincipal PawAuthUser authUser) {

        // Don't allow logged in users
        if (authUser != null)
            return new ModelAndView("redirect:/");

        ModelAndView mv = new ModelAndView("login");
        mv.addObject("loginForm", new LoginForm());
        return mv;
    }

    @RequestMapping(value = "/register")
    public ModelAndView register(
        @AuthenticationPrincipal PawAuthUser authUser,
        @ModelAttribute RegisterForm form
        ) {

        // Don't allow logged in users
        if (authUser != null)
            return new ModelAndView("redirect:/");

        ModelAndView mv = new ModelAndView("register");
        mv.addObject("registerForm", form);
        return mv;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView createUser(
        @Valid @ModelAttribute RegisterForm form,
        final BindingResult errors
        ) {

        ModelAndView mv = new ModelAndView("register");
        mv.addObject("registerForm", form);

        if (errors.hasErrors()) {
            return mv;
        }

        LOGGER.atDebug().addArgument(form.getEmail()).log("About to attempt register email {}");

        if (userService.findByEmail(form.getEmail()).isPresent()) {
            LOGGER.atDebug().addArgument(form.getEmail()).log("The email {} is already in use");

            errors.rejectValue("email", "EmailInUse.authForm.email");
            return mv;
        }

        final User user = userService.createUser(
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

        final PawAuthUser authUser = new PawAuthUser(user);

        final Authentication auth = new UsernamePasswordAuthenticationToken(
            authUser,
            null,
            authUser.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        verificationTokenService.createVerificationTokenForUser(user.getId());
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "/profile")
    public ModelAndView profile(
        @AuthenticationPrincipal PawAuthUser authUser,
        @RequestParam(value = "userId", required = false) final Long userId,
        @RequestParam(value = "page", defaultValue = "1") final int page
    ) {
        final boolean isOwnProfile;
        final User profileUser;

        if (userId != null) {
            profileUser = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            isOwnProfile = (authUser != null && authUser.getUser().getId().equals(userId));
        } else {
            if (authUser == null) {
                return new ModelAndView("redirect:/login");
            }
            profileUser = userService.findById(authUser.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
            isOwnProfile = true;
        }

        final ModelAndView mv = new ModelAndView("profile");
        enrichProfileModel(mv, profileUser, isOwnProfile, authUser, page);
        if (isOwnProfile) {
            mv.addObject("userProfileForm", UserProfileForm.fromUser(profileUser));
        }
        return mv;
    }

    @RequestMapping(value = "/profile/update", method = RequestMethod.POST)
    public ModelAndView updateProfile(
        @AuthenticationPrincipal PawAuthUser authUser,
        @Valid @ModelAttribute("userProfileForm") final UserProfileForm form,
        final BindingResult errors
    ) {
        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final User profileUser = userService.findById(authUser.getUser().getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));

        if (errors.hasErrors()) {
            final ModelAndView mv = new ModelAndView("profile");
            enrichProfileModel(mv, profileUser, true, authUser, 1);
            mv.addObject("userProfileForm", form);
            return mv;
        }

        userService.updateUserProfile(
            profileUser.getId(),
            form.getFirstName(),
            form.getLastName(),
            form.getStreetName(),
            form.getStreetNumber(),
            form.getNeighborhood(),
            form.getProvince(),
            form.getExtraAddressInfo(),
            form.getCbuCvu());

        final User refreshed = userService.findById(profileUser.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        refreshAuthenticationPrincipal(authUser, refreshed);

        return new ModelAndView("redirect:/profile?tab=mydata&updated=1");
    }

    private static void refreshAuthenticationPrincipal(final PawAuthUser current, final User refreshedUser) {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        final PawAuthUser newPrincipal = new PawAuthUser(
            refreshedUser.getEmail(),
            refreshedUser.getPassword(),
            current.isEnabled(),
            current.isAccountNonExpired(),
            current.isCredentialsNonExpired(),
            current.isAccountNonLocked(),
            new ArrayList<>(current.getAuthorities()),
            refreshedUser
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                newPrincipal,
                currentAuth != null ? currentAuth.getCredentials() : null,
                newPrincipal.getAuthorities()));
    }

    private void enrichProfileModel(
        final ModelAndView mv,
        final User profileUser,
        final boolean isOwnProfile,
        final PawAuthUser authUser,
        final int page
    ) {
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
            10
        );

        final PaginatedResult<Product> productsPage = productService.listProducts(criteria);

        final Map<Long, String> productImageUrls = new HashMap<>();
        for (Product product : productsPage.getResults()) {
            if (imageService.existsByProductId(product.getId())) {
                productImageUrls.put(product.getId(), "/images/product/" + product.getId());
            }
        }

        mv.addObject("user", profileUser);
        mv.addObject("isOwnProfile", isOwnProfile);
        mv.addObject("userProductsPage", productsPage);
        mv.addObject("userProducts", productsPage.getResults());
        mv.addObject("productImageUrls", productImageUrls);

        PaginatedResult<ar.edu.itba.paw.models.Review> reviewsPage = reviewService.findBySellerId(profileUser.getId(), page, 10);
        mv.addObject("receivedReviewsPage", reviewsPage);
        mv.addObject("receivedReviews", reviewsPage.getResults());
        mv.addObject("sellerRating", reviewService.summaryForSeller(profileUser.getId()));

        if (isOwnProfile && authUser != null) {
            final PaginatedResult<Purchase> purchasesPage = purchaseService.findByBuyerId(profileUser.getId(), page, 10);

            final Map<Long, Product> purchaseProducts = new HashMap<>();
            final Map<Long, Boolean> purchaseHasReview = new HashMap<>();
            for (Purchase p : purchasesPage.getResults()) {
                productService.findById(p.getProductId()).ifPresent(prod ->
                    purchaseProducts.put(p.getPurchaseId(), prod)
                );
                purchaseHasReview.put(p.getPurchaseId(), reviewService.findByPurchaseId(p.getPurchaseId()).isPresent());
            }

            mv.addObject("purchasesPage", purchasesPage);
            mv.addObject("purchases", purchasesPage.getResults());
            mv.addObject("purchaseProducts", purchaseProducts);
            mv.addObject("purchaseHasReview", purchaseHasReview);

            final PaginatedResult<Purchase> salesPage = purchaseService.findBySellerId(profileUser.getId(), page, 10);

            final Map<Long, Product> saleProducts = new HashMap<>();
            for (Purchase s : salesPage.getResults()) {
                productService.findById(s.getProductId()).ifPresent(prod ->
                    saleProducts.put(s.getPurchaseId(), prod)
                );
            }

            mv.addObject("salesPage", salesPage);
            mv.addObject("sales", salesPage.getResults());
            mv.addObject("saleProducts", saleProducts);

            // Load reports for admins (checked via Spring Security role)
            if (authUser != null && authUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                final List<ar.edu.itba.paw.models.ReportedProduct> reportedProducts = reportService.findAllGroupedByProduct();
                mv.addObject("reportedProducts", reportedProducts);
            }
        }
    }

    @RequestMapping(value = "/profile/admin/hide-product", method = RequestMethod.POST)
    public ModelAndView adminHideProduct(
        @RequestParam("productId") final Long productId
    ) {
        // Authorization enforced by Spring Security: only ROLE_ADMIN can reach this endpoint
        productService.hideProductFromCatalog(productId);
        reportService.deleteByProductId(productId);
        return new ModelAndView("redirect:/profile?tab=reports&hidden=1");
    }

    @RequestMapping(value = "/profile/admin/ban-user", method = RequestMethod.POST)
    public ModelAndView adminBanUser(
        @RequestParam("userId") final Long userId
    ) {
        // Authorization enforced by Spring Security: only ROLE_ADMIN can reach this endpoint

        // Ban the user
        userService.ban(userId);

        // Hide all their active products
        final ProductSearchCriteria criteria = new ProductSearchCriteria(
            null, Collections.emptyList(), null, null,
            Collections.emptyList(), Collections.emptyList(), null, userId,
            1, 1000000
        );
        final List<Product> userProducts = productService.listProducts(criteria).getResults();
        for (Product p : userProducts) {
            productService.hideProductFromCatalog(p.getId());
            reportService.deleteByProductId(p.getId());
        }

        return new ModelAndView("redirect:/profile?tab=reports&banned=1");
    }
}
