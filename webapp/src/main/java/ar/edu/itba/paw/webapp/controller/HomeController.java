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
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import ar.edu.itba.paw.models.ConditionBucket;
import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;
import ar.edu.itba.paw.models.ProductSortOrder;
import ar.edu.itba.paw.models.SellerRatingSummary;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.CategoryService;
import ar.edu.itba.paw.services.ImageService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;

@Controller
public class HomeController {

	private final ImageService imageService;
	private final ProductService productService;
	private final CategoryService categoryService;
    private final UserService userService;
    private final ReviewService reviewService;

	@Autowired
	public HomeController(
		final ProductService productService,
		final ImageService imageService,
		final CategoryService categoryService,
		final UserService userService,
		final ReviewService reviewService
	) {
		this.productService = productService;
		this.imageService = imageService;
		this.categoryService = categoryService;
		this.userService = userService;
		this.reviewService = reviewService;
	}

	private static BigDecimal parsePriceParam(final String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String normalized = raw.trim();
		if (normalized.contains(",")) {
			normalized = normalized.replace(".", "").replace(",", ".");
		} else if (normalized.matches("\\d{1,3}(\\.\\d{3})+")) {
			normalized = normalized.replace(".", "");
		}
		if (normalized.endsWith(".")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		try {
			return new BigDecimal(normalized);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView home(
        @AuthenticationPrincipal PawAuthUser authUser,
		@RequestParam(value = "search-text", required = false) final String searchText,
		@RequestParam(value = "categories", required = false) final List<Long> categoryIds,
		@RequestParam(value = "minPrice", required = false) final String minPriceParam,
		@RequestParam(value = "maxPrice", required = false) final String maxPriceParam,
		@RequestParam(value = "label", required = false) final List<String> recordLabels,
		@RequestParam(value = "estado", required = false) final List<String> estadoParams,
		@RequestParam(value = "sort", required = false) final String sortParam,
		@RequestParam(value = "page", defaultValue = "1") final int page
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

		final String trimmedSearch = searchText != null ? searchText.trim() : "";
		final boolean hasActiveSearch = !trimmedSearch.isEmpty();
		final boolean hasActiveFilters = hasActiveSearch
			|| (categoryIds != null && !categoryIds.isEmpty())
			|| minPrice != null
			|| maxPrice != null
			|| (recordLabels != null && !recordLabels.isEmpty())
			|| !buckets.isEmpty();

		final ProductSortOrder sortOrder = ProductSortOrder.parse(sortParam).orElse(ProductSortOrder.NEWEST);

		final ProductSearchCriteria criteria = new ProductSearchCriteria(
			hasActiveSearch ? trimmedSearch : null,
			categoryIds,
			minPrice,
			maxPrice,
			recordLabels,
			buckets,
			sortOrder,
			null,
			page,
			12
		);

		final PaginatedResult<Product> productsPage = productService.listProducts(criteria);
		final Map<Long, String> productImageUrls = new HashMap<>();

		for (Product product : productsPage.getResults()) {
			if (imageService.existsByProductId(product.getId())) {
				productImageUrls.put(product.getId(), "/images/product/" + product.getId());
			}
		}

		final Set<Long> distinctSellerIds = new HashSet<>();
		for (Product product : productsPage.getResults()) {
			distinctSellerIds.add(product.getUserId());
		}
		final Map<Long, SellerRatingSummary> sellerRatingByUserId = new HashMap<>();
		for (Long sellerId : distinctSellerIds) {
			sellerRatingByUserId.put(sellerId, reviewService.summaryForSeller(sellerId));
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

		// If password was never changed, tell the user
		if (authUser != null && authUser.getUser() != null && userService.isPasswordEmpty(authUser.getUser())) {
			mav.addObject("changePsswdModal", true);
		} else {
			mav.addObject("changePsswdModal", false);
		}

		mav.addObject("productsPage", productsPage);
		mav.addObject("products", productsPage.getResults());
		mav.addObject("productImageUrls", productImageUrls);
		mav.addObject("sellerRatingByUserId", sellerRatingByUserId);
		mav.addObject("categories", categoryService.findAll());
		mav.addObject("recordLabelsFilter", productService.listDistinctRecordLabels());
		mav.addObject("selectedCategoryIds", selectedCategoryIds);
		mav.addObject("selectedLabels", selectedLabels);
		mav.addObject("selectedEstados", selectedEstados);
		mav.addObject("filterMinPrice", minPriceParam != null ? minPriceParam : "");
		mav.addObject("filterMaxPrice", maxPriceParam != null ? maxPriceParam : "");
		mav.addObject("sortOptions", ProductSortOrder.values());
		mav.addObject("selectedSort", sortOrder.name());
		mav.addObject("activeSearchText", hasActiveSearch ? trimmedSearch : null);
		mav.addObject("noProductsMatchFilters", productsPage.getResults().isEmpty() && hasActiveFilters);
		return mav;
	}
}
