package ar.edu.itba.paw.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.services.ProductService;

@Controller
public class HomeController {

	private final ProductService productService;

	@Autowired
	public HomeController(final ProductService productService) {
		this.productService = productService;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView home() {
		final ModelAndView mav = new ModelAndView("home");
		mav.addObject("products", productService.listProducts());
		return mav;
	}
}
