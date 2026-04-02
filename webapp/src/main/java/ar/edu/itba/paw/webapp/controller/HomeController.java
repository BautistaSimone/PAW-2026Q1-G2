package ar.edu.itba.paw.webapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;

@Controller
public class HomeController {

	private final ImageService imageService;
	private final ProductService productService;

	@Autowired
	public HomeController(final ProductService productService, final ImageService imageService) {
		this.productService = productService;
		this.imageService = imageService;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView home() {
		final List<Product> products = productService.listProducts();
		final Map<Long, String> productImageUrls = new HashMap<>();

		for (Product product : products) {
			if (imageService.existsByProductId(product.getId())) {
				productImageUrls.put(product.getId(), "/images/product/" + product.getId());
			}
		}

		final ModelAndView mav = new ModelAndView("home");
		mav.addObject("products", products);
		mav.addObject("productImageUrls", productImageUrls);
		return mav;
	}
}
