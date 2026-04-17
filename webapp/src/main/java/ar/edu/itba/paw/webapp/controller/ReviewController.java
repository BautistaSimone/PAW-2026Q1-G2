package ar.edu.itba.paw.webapp.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.PurchaseService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.form.ReviewForm;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public ReviewController(
        final ReviewService reviewService,
        final PurchaseService purchaseService,
        final ProductService productService,
        final UserService userService
    ) {
        this.reviewService = reviewService;
        this.purchaseService = purchaseService;
        this.productService = productService;
        this.userService = userService;
    }

    @RequestMapping(value = "/purchases/{id}/review", method = RequestMethod.GET)
    public ModelAndView showReviewForm(
        @PathVariable("id") final Long id,
        @RequestParam("token") final String token,
        @ModelAttribute("reviewForm") final ReviewForm form
    ) {
        final Purchase purchase = purchaseService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (!isBuyerToken(token, purchase)) {
            throw new IllegalArgumentException("Invalid token");
        }

        if (purchase.getStatus() != PurchaseStatus.DELIVERED) {
            return new ModelAndView("redirect:/purchases/" + id + "?token=" + token);
        }

        if (reviewService.findByPurchaseId(id).isPresent()) {
            return new ModelAndView("redirect:/purchases/" + id + "?token=" + token + "&reviewed=1");
        }

        final Product product = productService.findById(purchase.getProductId())
            .orElseThrow(() -> new IllegalStateException("Product not found"));

        final User seller = userService.findById(product.getUserId())
            .orElseThrow(() -> new IllegalStateException("Seller not found"));

        ModelAndView mav = new ModelAndView("review-form");
        mav.addObject("purchase", purchase);
        mav.addObject("product", product);
        mav.addObject("seller", seller);
        mav.addObject("token", token);
        return mav;
    }

    @RequestMapping(value = "/purchases/{id}/review", method = RequestMethod.POST)
    public ModelAndView submitReview(
        @PathVariable("id") final Long id,
        @RequestParam("token") final String token,
        @Valid @ModelAttribute("reviewForm") final ReviewForm form,
        final BindingResult errors
    ) {
        final Purchase purchase = purchaseService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (!isBuyerToken(token, purchase)) {
            throw new IllegalArgumentException("Invalid token");
        }

        if (errors.hasErrors()) {
            final Product product = productService.findById(purchase.getProductId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));
            final User seller = userService.findById(product.getUserId())
                .orElseThrow(() -> new IllegalStateException("Seller not found"));

            ModelAndView mav = new ModelAndView("review-form");
            mav.addObject("purchase", purchase);
            mav.addObject("product", product);
            mav.addObject("seller", seller);
            mav.addObject("token", token);
            return mav;
        }

        reviewService.create(id, purchase.getBuyerId(), form.getScore(), form.getText());

        return new ModelAndView("redirect:/purchases/" + id + "?token=" + token + "&reviewed=1");
    }

    private static boolean isBuyerToken(String token, Purchase purchase) {
        return MessageDigest.isEqual(
            token.getBytes(StandardCharsets.UTF_8),
            purchase.getBuyerToken().getBytes(StandardCharsets.UTF_8)
        );
    }
}
