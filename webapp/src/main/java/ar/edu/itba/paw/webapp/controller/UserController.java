package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;
import java.io.File;

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

import java.util.Arrays;
import java.util.Collection;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.forms.RegisterForm;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.models.User;


@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

	@RequestMapping(value = "/login")
	public ModelAndView login() {
		return new ModelAndView("login");
	}

	@RequestMapping(value = "/register")
	public ModelAndView register(@ModelAttribute RegisterForm form) {

        // Return the form too so the user won't have to write it again
        return new ModelAndView("login");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView createUser(@ModelAttribute RegisterForm form, 
        final BindingResult errors) {

        if (errors.hasErrors()) {
            return register(form);
        }

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
        Authentication auth = new UsernamePasswordAuthenticationToken(authUser, authUser.getPassword());

        SecurityContextHolder.getContext().setAuthentication(auth);

        return new ModelAndView("redirect:/");
    }
}
