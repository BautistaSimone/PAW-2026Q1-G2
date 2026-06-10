package ar.edu.itba.paw.webapp.controller;

import java.util.Optional;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.PasswordTokenService;
import ar.edu.itba.paw.webapp.form.UpdatePasswordForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.Util;
import ar.edu.itba.paw.models.User;

@Controller
public class PasswordController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordController.class);

    private final UserService userService;
    private final PasswordTokenService passwordTokenService;

    @Autowired
    public PasswordController(
            final UserService userService,
            final PasswordTokenService passwordTokenService) {

        this.userService = userService;
        this.passwordTokenService = passwordTokenService;
    }

    @RequestMapping(value = "/resetPassword")
    public ModelAndView showResetPasswordPage(
            @AuthenticationPrincipal PawAuthUser authUser) {

        ModelAndView mv = new ModelAndView("forgot-password");

        // If logged in, fill in the mail
        if (authUser != null) {
            mv.addObject("userEmail", authUser.getUser().getEmail());
        }

        return mv;
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam("email") String userEmail,
            final RedirectAttributes redirectAttributes) {

        final Optional<User> userOpt = userService.findByEmail(userEmail);

        if (!userOpt.isPresent()) {
            ModelAndView mv = new ModelAndView("forgot-password");
            mv.addObject("error", "UserNotFound.authForm.email");
            return mv;
        }

        final User user = userOpt.get();

        passwordTokenService.createPasswordResetTokenForUser(user);

        redirectAttributes.addFlashAttribute("message", "EmailSent.authForm.email");

        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ModelAndView changePassword(
            @AuthenticationPrincipal PawAuthUser authUser,
            @Valid @ModelAttribute UpdatePasswordForm form,
            final BindingResult errors,
            final HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        // Return to the same page if an error occurs
        if (errors.hasErrors()) {
            ModelAndView mv = new ModelAndView("update-password");
            mv.addObject("updatePasswordForm", form);
            mv.addObject("productDetailBackUrl", Util.resolveBackUrl(request));
            return mv;
        }

        // If the user is logged in, there is no need for a token
        if (authUser != null) {
            userService.updatePassword(authUser.getUser().getId(), form.getNewPassword());
        } else if (!userService.resetPasswordWithToken(form.getToken(), form.getNewPassword())) {
            return new ModelAndView("redirect:/login");
        }

        // Reset the form on success
        form = new UpdatePasswordForm();

        if (authUser == null) {
            // If no user, go to login
            ModelAndView mv = new ModelAndView("redirect:/login");
            redirectAttributes.addFlashAttribute("message", "UpdatedPassword.authForm.password");
            return mv;
        }

        ModelAndView mv = new ModelAndView("redirect:/");
        redirectAttributes.addFlashAttribute("message", "UpdatedPassword.authForm.password");

        return mv;
    }

    @RequestMapping(value = "/changePassword")
    public ModelAndView showChangePasswordPage(
            @AuthenticationPrincipal PawAuthUser authUser,
            @ModelAttribute UpdatePasswordForm form,
            final HttpServletRequest request) {

        ModelAndView mv = new ModelAndView();
        mv.addObject("productDetailBackUrl", Util.resolveBackUrl(request));

        if (authUser == null && !passwordTokenService.isValidPasswordResetToken(form.getToken())) {

            mv.setViewName("redirect:/login");
            return mv;
        }

        mv.setViewName("update-password");
        mv.addObject("updatePasswordForm", form);

        return mv;
    }
}
