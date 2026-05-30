package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
import ar.edu.itba.paw.models.User;
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
			final ReviewService reviewService) {
		this.productService = productService;
		this.imageService = imageService;
		this.categoryService = categoryService;
		this.userService = userService;
		this.reviewService = reviewService;
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
			@RequestParam(value = "page", defaultValue = "1") final int page) {

		if (page < 1) {
			throw new IllegalArgumentException("Invalid page");
		}

		final ProductSearchCriteria criteria = productService.getProductSearchCriteria(
				searchText,
				categoryIds,
				minPriceParam,
				maxPriceParam,
				recordLabels,
				estadoParams,
				sortParam,
				page);

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

		final String trimmedSearch = searchText != null ? searchText.trim() : "";

		final boolean hasActiveSearch = !trimmedSearch.isEmpty();
		final boolean hasActiveFilters = hasActiveSearch
				|| (categoryIds != null && !categoryIds.isEmpty())
				|| criteria.getMinPrice() != null
				|| criteria.getMaxPrice() != null
				|| (recordLabels != null && !recordLabels.isEmpty())
				|| (estadoParams != null && !estadoParams.isEmpty());

		final ProductSortOrder sortOrder = ProductSortOrder.parse(sortParam).orElse(ProductSortOrder.NEWEST);

		final List<ConditionBucket> buckets = new ArrayList<>();
		if (estadoParams != null) {
			for (String raw : estadoParams) {
				ConditionBucket.parse(raw).ifPresent(buckets::add);
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
		mav.addObject("selectedCategoryIds", selectedCategoryIds);
		mav.addObject("selectedLabels", selectedLabels);
		mav.addObject("selectedEstados", selectedEstados);
		mav.addObject("filterMinPrice", minPriceParam != null ? minPriceParam : "");
		mav.addObject("filterMaxPrice", maxPriceParam != null ? maxPriceParam : "");
		mav.addObject("sortOptions", ProductSortOrder.values());
		mav.addObject("selectedSort", sortOrder.name());
		mav.addObject("activeSearchText", hasActiveSearch ? trimmedSearch : null);
		mav.addObject("hasActiveFilters", hasActiveFilters);
		mav.addObject("noProductsMatchFilters", productsPage.getResults().isEmpty() && hasActiveFilters);
		return mav;
	}

	@RequestMapping(value = "/for-you", method = RequestMethod.GET)
	public ModelAndView forYou(
			@AuthenticationPrincipal PawAuthUser authUser,
			@RequestParam(value = "page", defaultValue = "1") final int page,
			@RequestParam(value = "wishlistPage", defaultValue = "1") final int wishlistPage) {

		if (authUser == null) {
			return new ModelAndView("redirect:/login");
		}

		if (page < 1 || wishlistPage < 1) {
			throw new IllegalArgumentException("Invalid page");
		}

		final Long currentUserId = authUser.getUser().getId();
		final List<Long> followedIds = userService.getFollowedUserIds(currentUserId);

		final ModelAndView mav = new ModelAndView("forYou");
		mav.addObject("hasFollowing", !followedIds.isEmpty());
		mav.addObject("currentUserId", currentUserId);

		PaginatedResult<Product> productsPage = new PaginatedResult<>(Collections.emptyList(), page, 12, 0);
		final Map<Long, String> productImageUrls = new HashMap<>();
		Map<Long, SellerRatingSummary> sellerRatingByUserId = new HashMap<>();
		if (!followedIds.isEmpty()) {
			final ProductSearchCriteria criteria = new ProductSearchCriteria(
					null,
					Collections.emptyList(),
					null,
					null,
					Collections.emptyList(),
					Collections.emptyList(),
					ProductSortOrder.NEWEST,
					null,
					followedIds,
					page,
					12
			);

			productsPage = productService.listProducts(criteria);

			for (Product product : productsPage.getResults()) {
				if (imageService.existsByProductId(product.getId())) {
					productImageUrls.put(product.getId(), "/images/product/" + product.getId());
				}
			}

			final Set<Long> distinctSellerIds = new HashSet<>();
			for (Product product : productsPage.getResults()) {
				distinctSellerIds.add(product.getUserId());
			}

			sellerRatingByUserId = reviewService.sellerRatingByUserId(distinctSellerIds);
		}

		mav.addObject("productsPage", productsPage);
		mav.addObject("products", productsPage.getResults());
		mav.addObject("productImageUrls", productImageUrls);
		mav.addObject("sellerRatingByUserId", sellerRatingByUserId);

		final PaginatedResult<Product> wishlistProductsPage =
				productService.getRecommendedProductsPage(currentUserId, wishlistPage, 12, null);
		final Map<Long, String> wishlistProductImageUrls = new HashMap<>();
		final Map<Long, SellerRatingSummary> wishlistSellerRatingByUserId = new HashMap<>();
		if (!wishlistProductsPage.getResults().isEmpty()) {
			for (Product product : wishlistProductsPage.getResults()) {
				if (imageService.existsByProductId(product.getId())) {
					wishlistProductImageUrls.put(product.getId(), "/images/product/" + product.getId());
				}
			}

			final Set<Long> distinctWishlistSellers = new HashSet<>();
			for (Product product : wishlistProductsPage.getResults()) {
				distinctWishlistSellers.add(product.getUserId());
			}
			for (Long sellerId : distinctWishlistSellers) {
				wishlistSellerRatingByUserId.put(sellerId, reviewService.summaryForSeller(sellerId));
			}
		}
		mav.addObject("wishlistProductsPage", wishlistProductsPage);
		mav.addObject("wishlistProducts", wishlistProductsPage.getResults());
		mav.addObject("wishlistProductImageUrls", wishlistProductImageUrls);
		mav.addObject("wishlistSellerRatingByUserId", wishlistSellerRatingByUserId);

		return mav;
	}

	@RequestMapping(value = "/search-users", method = RequestMethod.GET)
	public ModelAndView searchUsers(
			@AuthenticationPrincipal PawAuthUser authUser,
			@RequestParam(value = "q", required = false) final String query,
			@RequestParam(value = "page", defaultValue = "1") final int page) {

		if (page < 1) {
			throw new IllegalArgumentException("Invalid page");
		}

		final ModelAndView mav = new ModelAndView("searchUsers");

		final boolean hasQuery = query != null && !query.trim().isEmpty();
		mav.addObject("searchQuery", hasQuery ? query.trim() : "");

		if (hasQuery) {
			final PaginatedResult<User> usersPage = userService.searchUsers(query.trim(), page, 12);
			mav.addObject("usersPage", usersPage);
			mav.addObject("users", usersPage.getResults());
			mav.addObject("showingSearchResults", true);
		} else {
			final List<User> topUsers = userService.getMostFollowedUsers(12);
			mav.addObject("users", topUsers);
			mav.addObject("showingSearchResults", false);
		}

		if (authUser != null) {
			final List<User> displayedUsers = (List<User>) mav.getModel().get("users");
			final Map<Long, Boolean> followStatusMap = new HashMap<>();
			final Map<Long, Long> userFollowerCounts = new HashMap<>();
			if (displayedUsers != null) {
				for (User u : displayedUsers) {
					followStatusMap.put(u.getId(), userService.isFollowing(authUser.getUser().getId(), u.getId()));
					userFollowerCounts.put(u.getId(), userService.countFollowers(u.getId()));
				}
			}
			mav.addObject("followStatusMap", followStatusMap);
			mav.addObject("userFollowerCounts", userFollowerCounts);
			mav.addObject("currentUserId", authUser.getUser().getId());
		} else {
			final List<User> displayedUsers = (List<User>) mav.getModel().get("users");
			final Map<Long, Long> userFollowerCounts = new HashMap<>();
			if (displayedUsers != null) {
				for (User u : displayedUsers) {
					userFollowerCounts.put(u.getId(), userService.countFollowers(u.getId()));
				}
			}
			mav.addObject("userFollowerCounts", userFollowerCounts);
		}

		return mav;
	}
}
