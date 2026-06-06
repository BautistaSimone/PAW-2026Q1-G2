package ar.edu.itba.paw.webapp.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.services.ReviewContext;
import ar.edu.itba.paw.services.ReviewEligibility;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.form.ReviewForm;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(final ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/purchases/{id:\\d+}/review", method = RequestMethod.GET)
    public ModelAndView showReviewForm(
            @AuthenticationPrincipal PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @ModelAttribute("reviewForm") final ReviewForm form) {

        final ReviewEligibility eligibility = reviewService.getReviewEligibility(id, authUser.getUser().getId());
        final ModelAndView redirect = redirectIfUnavailable(eligibility, id);
        if (redirect != null) {
            return redirect;
        }
        return reviewFormView(eligibility.getContext());
    }

    @RequestMapping(value = "/purchases/{id:\\d+}/review", method = RequestMethod.POST)
    public ModelAndView submitReview(
            @AuthenticationPrincipal PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @Valid @ModelAttribute("reviewForm") final ReviewForm form,
            final BindingResult errors) {

        final ReviewEligibility eligibility = reviewService.getReviewEligibility(id, authUser.getUser().getId());
        final ModelAndView redirect = redirectIfUnavailable(eligibility, id);
        if (redirect != null) {
            return redirect;
        }

        if (errors.hasErrors()) {
            return reviewFormView(eligibility.getContext());
        }

        reviewService.create(id, authUser.getUser().getId(), form.getScore(), form.getText());

        return new ModelAndView("redirect:/purchases/" + id + "?reviewed=1");
    }

    private ModelAndView redirectIfUnavailable(final ReviewEligibility eligibility, final Long purchaseId) {
        if (eligibility.getStatus() == ReviewEligibility.Status.NOT_BUYER) {
            throw new SecurityException("Review is not available for the current user");
        }
        if (eligibility.getStatus() == ReviewEligibility.Status.NOT_DELIVERED) {
            return new ModelAndView("redirect:/purchases/" + purchaseId);
        }
        if (eligibility.getStatus() == ReviewEligibility.Status.ALREADY_REVIEWED) {
            return new ModelAndView("redirect:/purchases/" + purchaseId + "?reviewed=1");
        }
        return null;
    }

    private ModelAndView reviewFormView(final ReviewContext context) {
        final ModelAndView mav = new ModelAndView("review-form");
        mav.addObject("purchase", context.getPurchase());
        mav.addObject("product", context.getProduct());
        mav.addObject("seller", context.getSeller());
        return mav;
    }
}
