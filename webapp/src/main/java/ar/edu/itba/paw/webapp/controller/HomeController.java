package ar.edu.itba.paw.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.models.User;

@Controller
public class HomeController {
	
	private UserService userService;

	@Autowired
	public HomeController(final UserService userService) {
		this.userService = userService;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView home() {
		final ModelAndView mav = new ModelAndView("home");
		mav.addObject("message", "Hihihihihi");
		return mav;
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ModelAndView createUser(
		@RequestParam("email") final String email, 
		@RequestParam("password") final String password, 
		@RequestParam("username") final String username) {
		final ModelAndView mav = new ModelAndView("home");
		
		User user = userService.createUser(email, password, username);

		return mav;
	}
}
