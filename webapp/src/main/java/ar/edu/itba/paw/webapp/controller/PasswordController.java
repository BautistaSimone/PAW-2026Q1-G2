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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.PasswordTokenService;
import ar.edu.itba.paw.webapp.form.UpdatePasswordForm;
import ar.edu.itba.paw.webapp.form.LoginForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.PasswordToken;

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
        @AuthenticationPrincipal PawAuthUser authUser,
        @Valid @ModelAttribute UpdatePasswordForm form,
        final BindingResult errors,
        RedirectAttributes redirectAttributes
        ) {

        // Return to the same page if an error occurs
        if (errors.hasErrors()) {
            ModelAndView mv = new ModelAndView("update-password");
            mv.addObject("updatePasswordForm", form);
            return mv;
        }

        if (!passwordTokenService.isValidPasswordResetToken(form.getToken())) {
            ModelAndView mv = new ModelAndView("redirect:/login");
            return mv;
        }

        final Optional<PasswordToken> passTokenOpt = passwordTokenService.findByToken(form.getToken());

        // We already know it exists
        final PasswordToken passToken = passTokenOpt.get();

        userService.updatePassword(passToken.getUserId(), form.getNewPassword());

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
        @ModelAttribute UpdatePasswordForm form) {

        ModelAndView mv = new ModelAndView();

        if(!passwordTokenService.isValidPasswordResetToken(form.getToken())) {
            
            mv.setViewName("redirect:/login");
            return mv;
        }

        mv.setViewName("update-password");
        mv.addObject("updatePasswordForm", form);

        return mv;
    }
}
