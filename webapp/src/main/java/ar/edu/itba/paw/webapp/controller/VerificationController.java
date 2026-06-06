package ar.edu.itba.paw.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ar.edu.itba.paw.services.VerificationTokenService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.Util;
import ar.edu.itba.paw.models.User;

@Controller
public class VerificationController {

    private final VerificationTokenService verificationTokenService;

    @Autowired
    public VerificationController(
            final VerificationTokenService verificationTokenService) {

        this.verificationTokenService = verificationTokenService;
    }

    @RequestMapping(value = "/sendVerificationEmail", method = RequestMethod.POST)
    public ModelAndView verificationEmail(@AuthenticationPrincipal PawAuthUser authUser) {

        ModelAndView mv = new ModelAndView("redirect:/sendVerificationEmail");
        final User user = authUser.getUser();

        verificationTokenService.createVerificationTokenForUser(user.getId());

        mv.addObject("message", "EmailSent.authForm.email");

        return mv;
    }

    // TODO: Should this be always accessible?
    @RequestMapping(value = "/sendVerificationEmail")
    public ModelAndView showVerificationEmailSent() {

        ModelAndView mv = new ModelAndView("verification-email-sent");

        return mv;
    }

    @RequestMapping(value = "/notVerified")
    public ModelAndView showNotVerified() {

        ModelAndView mv = new ModelAndView("account-not-verified");

        return mv;
    }

    @RequestMapping(value = "/verifyEmail")
    public ModelAndView verifyEmail(
            @AuthenticationPrincipal PawAuthUser authUser,
            @RequestParam("token") final String token,
            RedirectAttributes redirectAttributes) {

        ModelAndView mv = new ModelAndView("redirect:/verificationStatus");

        final java.util.Optional<User> verifiedUser = verificationTokenService.verifyEmail(token);
        if (!verifiedUser.isPresent()) {
            redirectAttributes.addFlashAttribute("verificationSuccessful", false);
            redirectAttributes.addFlashAttribute("message", "ExpiredToken.verification");
            return mv;
        }

        redirectAttributes.addFlashAttribute("verificationSuccessful", true);
        redirectAttributes.addFlashAttribute("message", "SuccessToken.verification");

        // Update the current session too if the user is logged in
        if (authUser != null && authUser.getUser().getId().equals(verifiedUser.get().getId())) {
            Util.refreshAuthenticationPrincipal(authUser, verifiedUser.get());
        }

        return mv;
    }

    @RequestMapping(value = "/verificationStatus")
    public ModelAndView verificationStatus() {
        return new ModelAndView("verification-status");
    }
}
