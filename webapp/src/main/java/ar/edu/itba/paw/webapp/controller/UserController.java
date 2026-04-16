package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;
import java.io.File;
import java.util.Collections;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;

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
import ar.edu.itba.paw.webapp.forms.RegisterForm;
import ar.edu.itba.paw.webapp.forms.LoginForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.ProductSearchCriteria;


@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public UserController(final UserService userService, final ProductService productService) {
        this.userService = userService;
        this.productService = productService;
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

        if (errors.hasErrors()) {
            return new ModelAndView("register");
        }

        LOGGER.atDebug().addArgument(form.getEmail()).log("About to attempt register email {}");

        final User user = userService.createUser(form.getEmail(), form.getPassword(), form.getUsername(), false);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        PawAuthUser authUser = new PawAuthUser(
            user.getEmail(), 
            user.getPassword(),
            true,   // enabled
            true,   // accountNonExpired
            true,   // credentialsNonExpired
            true,   // accountNonLocked
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
    public ModelAndView profile(@AuthenticationPrincipal PawAuthUser authUser) {

        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        User user = authUser.getUser();

        ModelAndView mv = new ModelAndView("profile");

        // Get the products corresponding to out user
		final ProductSearchCriteria criteria = new ProductSearchCriteria(
            null,
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            user.getId()
		);

        mv.addObject("user", user);
        mv.addObject("userProducts", productService.listProducts(criteria));
        //mv.addObject("productImageUrls", userService.getUserProductImages(user.getId()));

        return mv;
    }
}
