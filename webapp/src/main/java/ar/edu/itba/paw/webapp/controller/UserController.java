package ar.edu.itba.paw.webapp.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.PurchaseService;
import ar.edu.itba.paw.services.PasswordTokenService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.LoginForm;
import ar.edu.itba.paw.webapp.form.UserProfileForm;
import ar.edu.itba.paw.webapp.form.UpdatePasswordForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.PasswordToken;
import ar.edu.itba.paw.models.ProductSearchCriteria;

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ProductService productService;
    private final ImageService imageService;
    private final PurchaseService purchaseService;
    private final ReviewService reviewService;
    private final PasswordTokenService passwordTokenService;

    @Autowired
    public UserController(
        final UserService userService,
        final ProductService productService,
        final ImageService imageService,
        final PurchaseService purchaseService,
        final ReviewService reviewService,
        final PasswordTokenService passwordTokenService) {

        this.userService = userService;
        this.productService = productService;
        this.imageService = imageService;
        this.purchaseService = purchaseService;
        this.reviewService = reviewService;
        this.passwordTokenService = passwordTokenService;
    }

    @RequestMapping(value = "/resetPassword")
    public ModelAndView showResetPasswordPage(
        @AuthenticationPrincipal PawAuthUser authUser
        ) {

        ModelAndView mv = new ModelAndView("forgot-password");

        // If logged in, fill in the mail
        if (authUser != null) {
            mv.addObject("userEmail", authUser.getUser().getEmail());
        }

        return mv;
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam("email") String userEmail) {

        final Optional<User> userOpt = userService.findByEmail(userEmail);

        if (!userOpt.isPresent()) {
            ModelAndView mv = new ModelAndView("forgot-password");
            mv.addObject("error", "UserNotFound.authForm.email");
            return mv;
        }

        ModelAndView mv = new ModelAndView("login");
        mv.addObject("loginForm", new LoginForm());

        final User user = userOpt.get();

        final String token = UUID.randomUUID().toString();

        passwordTokenService.createPasswordResetTokenForUser(user.getId(), token);

        mv.addObject("message", "EmailSent.authForm.email");

        return mv;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ModelAndView changePassword(
        @RequestParam("token") String token,
        @Valid @ModelAttribute UpdatePasswordForm form
        ) {

        ModelAndView mv = new ModelAndView();

        if (!passwordTokenService.isValidPasswordResetToken(token)) {
            mv.setViewName("redirect:/login");
            return mv;
        }

        final Optional<PasswordToken> passTokenOpt = passwordTokenService.findByToken(token);

        // We already know it exists
        final PasswordToken passToken = passTokenOpt.get();

        userService.updatePassword(passToken.getUserId(), form.getNewPassword());

        mv.setViewName("redirect:/login");
        mv.addObject("message", "Password updated successfully");

        return mv;
    }

    @RequestMapping(value = "/changePassword")
    public ModelAndView showChangePasswordPage(@RequestParam("token") String token) {

        ModelAndView mv = new ModelAndView();

        if(!passwordTokenService.isValidPasswordResetToken(token)) {
            
            mv.setViewName("redirect:/login");
            return mv;
        }

        mv.setViewName("update-password");
        mv.addObject("token", token);

        return mv;
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
    public ModelAndView createUser(@Valid @ModelAttribute RegisterForm form,
        final BindingResult errors) {

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
                form.getFirstName(),
                form.getLastName(),
                form.getStreetName(),
                form.getStreetNumber(),
                form.getNeighborhood(),
                form.getProvince(),
                form.getExtraAddressInfo(),
                form.getCbuCvu());

        Collection<? extends GrantedAuthority> authorities =
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        PawAuthUser authUser = new PawAuthUser(
            user.getEmail(),
            user.getPassword(),
            true,
            true,
            true,
            true,
            authorities,
            user
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
            authUser,
            null,
            authorities
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "/profile")
    public ModelAndView profile(
        @AuthenticationPrincipal PawAuthUser authUser,
        @RequestParam(value = "userId", required = false) final Long userId
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
        enrichProfileModel(mv, profileUser, isOwnProfile, authUser);
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
            enrichProfileModel(mv, profileUser, true, authUser);
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
        final PawAuthUser authUser
    ) {
        final ProductSearchCriteria criteria = new ProductSearchCriteria(
            null,
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            profileUser.getId()
        );

        final List<Product> products = productService.listProducts(criteria);

        final Map<Long, String> productImageUrls = new HashMap<>();
        for (Product product : products) {
            if (imageService.existsByProductId(product.getId())) {
                productImageUrls.put(product.getId(), "/images/product/" + product.getId());
            }
        }

        mv.addObject("user", profileUser);
        mv.addObject("isOwnProfile", isOwnProfile);
        mv.addObject("userProducts", products);
        mv.addObject("productImageUrls", productImageUrls);

        mv.addObject("receivedReviews", reviewService.findBySellerId(profileUser.getId()));
        mv.addObject("sellerRating", reviewService.summaryForSeller(profileUser.getId()));

        if (isOwnProfile && authUser != null) {
            final List<Purchase> purchases = purchaseService.findByBuyerId(profileUser.getId());

            final Map<Long, Product> purchaseProducts = new HashMap<>();
            final Map<Long, Boolean> purchaseHasReview = new HashMap<>();
            for (Purchase p : purchases) {
                productService.findById(p.getProductId()).ifPresent(prod ->
                    purchaseProducts.put(p.getPurchaseId(), prod)
                );
                purchaseHasReview.put(p.getPurchaseId(), reviewService.findByPurchaseId(p.getPurchaseId()).isPresent());
            }

            mv.addObject("purchases", purchases);
            mv.addObject("purchaseProducts", purchaseProducts);
            mv.addObject("purchaseHasReview", purchaseHasReview);

            final List<Purchase> sales = purchaseService.findBySellerId(profileUser.getId());

            final Map<Long, Product> saleProducts = new HashMap<>();
            for (Purchase s : sales) {
                productService.findById(s.getProductId()).ifPresent(prod ->
                    saleProducts.put(s.getPurchaseId(), prod)
                );
            }

            mv.addObject("sales", sales);
            mv.addObject("saleProducts", saleProducts);
        }
    }
}
