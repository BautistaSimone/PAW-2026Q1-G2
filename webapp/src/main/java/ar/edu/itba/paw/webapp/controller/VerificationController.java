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
import ar.edu.itba.paw.services.VerificationTokenService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Token;

@Controller
public class VerificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationController.class);

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    @Autowired
    public VerificationController(
        final UserService userService,
        final VerificationTokenService verificationTokenService) {

        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
    }

    @RequestMapping(value = "verifyEmail", method = RequestMethod.POST)
    public ModelAndView verificationEmail(@AuthenticationPrincipal PawAuthUser authUser) {

        ModelAndView mv = new ModelAndView("login");
        if (authUser == null) {
            mv.addObject("error", "UserNotFound.authForm.email");
            return mv;
        }

        final User user = authUser.getUser();

        verificationTokenService.createVerificationTokenForUser(user.getId());

        mv.addObject("message", "EmailSent.authForm.email");

        return mv;
    }

    @RequestMapping(value = "verificationPending")
    public ModelAndView verificationPending() {

        ModelAndView mv = new ModelAndView("login");
        return mv;
    }

    @RequestMapping(value = "verificationStatus")
    public ModelAndView verificationStatus() {

        ModelAndView mv = new ModelAndView("login");
        return mv;
    }

}
