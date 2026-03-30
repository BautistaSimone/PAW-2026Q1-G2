package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;
import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.services.UserService;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }


    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public ModelAndView createUser(
        @RequestParam("email") final String email,
        @RequestParam("password") final String password,
        @RequestParam("username") final String username
    ) {

        userService.createUser(email, password, username, false);   // Not a moderator
        return new ModelAndView("redirect:/?created=1");
    }
}
