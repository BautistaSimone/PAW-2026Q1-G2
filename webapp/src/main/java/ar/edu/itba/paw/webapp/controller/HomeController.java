package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.ConditionBucket;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;

@Controller
public class HomeController {

	private final ImageService imageService;
	private final ProductService productService;
	private final CategoryService categoryService;

	@Autowired
	public HomeController(
		final ProductService productService,
		final ImageService imageService,
		final CategoryService categoryService
	) {
		this.productService = productService;
		this.imageService = imageService;
		this.categoryService = categoryService;
	}

	private static BigDecimal parsePriceParam(final String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return new BigDecimal(raw.trim());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView home(
		@RequestParam(value = "search_text", required = false) final String searchText,
		@RequestParam(value = "categories", required = false) final List<Long> categoryIds,
		@RequestParam(value = "minPrice", required = false) final String minPriceParam,
		@RequestParam(value = "maxPrice", required = false) final String maxPriceParam,
		@RequestParam(value = "label", required = false) final List<String> recordLabels,
		@RequestParam(value = "estado", required = false) final List<String> estadoParams
	) {
		BigDecimal minPrice = parsePriceParam(minPriceParam);
		BigDecimal maxPrice = parsePriceParam(maxPriceParam);
		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
			final BigDecimal tmp = minPrice;
			minPrice = maxPrice;
			maxPrice = tmp;
		}

		final List<ConditionBucket> buckets = new ArrayList<>();
		if (estadoParams != null) {
			for (String raw : estadoParams) {
				ConditionBucket.parse(raw).ifPresent(buckets::add);
			}
		}

		final ProductSearchCriteria criteria = new ProductSearchCriteria(
			searchText,
			categoryIds,
			minPrice,
			maxPrice,
			recordLabels,
			buckets
		);

		final List<Product> products = productService.listProducts(criteria);
		final Map<Long, String> productImageUrls = new HashMap<>();

		for (Product product : products) {
			if (imageService.existsByProductId(product.getId())) {
				productImageUrls.put(product.getId(), "/images/product/" + product.getId());
			}
		}

		final Set<Long> selectedCategoryIds = new HashSet<>();
		if (categoryIds != null) {
			selectedCategoryIds.addAll(categoryIds);
		}

		final Set<String> selectedLabels = new HashSet<>();
		if (recordLabels != null) {
			for (String label : recordLabels) {
				if (label != null && !label.isBlank()) {
					selectedLabels.add(label.trim());
				}
			}
		}

		final Set<String> selectedEstados = new HashSet<>();
		for (ConditionBucket b : buckets) {
			selectedEstados.add(b.name());
		}

		final ModelAndView mav = new ModelAndView("home");
		mav.addObject("products", products);
		mav.addObject("productImageUrls", productImageUrls);
		mav.addObject("categories", categoryService.findAll());
		mav.addObject("recordLabelsFilter", productService.listDistinctRecordLabels());
		mav.addObject("selectedCategoryIds", selectedCategoryIds);
		mav.addObject("selectedLabels", selectedLabels);
		mav.addObject("selectedEstados", selectedEstados);
		mav.addObject("filterMinPrice", minPriceParam != null ? minPriceParam : "");
		mav.addObject("filterMaxPrice", maxPriceParam != null ? maxPriceParam : "");
		return mav;
	}
}
