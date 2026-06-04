package ar.edu.itba.paw.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.VerificationTokenService;
import ar.edu.itba.paw.webapp.form.LoginForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.auth.PawUserDetailsService;
import ar.edu.itba.paw.webapp.Util;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.VerificationToken;

@Controller
public class VerificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationController.class);

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final PawUserDetailsService pawUserDetailsService;

    @Autowired
    public VerificationController(
            final UserService userService,
            final VerificationTokenService verificationTokenService,
            PawUserDetailsService pawUserDetailsService) {

        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.pawUserDetailsService = pawUserDetailsService;
    }

    @RequestMapping(value = "/sendVerificationEmail", method = RequestMethod.POST)
    public ModelAndView verificationEmail(@AuthenticationPrincipal PawAuthUser authUser) {

        if (authUser == null) {
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("loginForm", new LoginForm());
            mv.addObject("error", "UserNotFound.authForm.email");
            return mv;
        }

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

        if (!verificationTokenService.isValidVerificationToken(token)) {
            redirectAttributes.addFlashAttribute("verificationSuccessful", false);
            redirectAttributes.addFlashAttribute("message", "ExpiredToken.verification");
            return mv;
        }

        final VerificationToken verificationToken = verificationTokenService.findByToken(token).get();

        userService.enable(verificationToken.getUserId());

        redirectAttributes.addFlashAttribute("verificationSuccessful", true);
        redirectAttributes.addFlashAttribute("message", "SuccessToken.verification");

        // Update the current session too if the user is logged in
        if (authUser != null) {
            final User refreshed = userService.findById(authUser.getUser().getId())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            Util.refreshAuthenticationPrincipal(authUser, refreshed);
        }

        return mv;
    }

    @RequestMapping(value = "/verificationStatus")
    public ModelAndView verificationStatus() {
        return new ModelAndView("verification-status");
    }
}
