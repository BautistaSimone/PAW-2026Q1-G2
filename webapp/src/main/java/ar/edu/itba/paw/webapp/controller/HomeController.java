package ar.edu.itba.paw.webapp.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	private static final int COMMUNITY_USER_PAGE_SIZE = 6;
	private static final int COMMUNITY_PRODUCT_LIMIT = 8;

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
			@RequestParam(value = "minPrice", required = false) final BigDecimal minPrice,
			@RequestParam(value = "maxPrice", required = false) final BigDecimal maxPrice,
			@RequestParam(value = "label", required = false) final List<String> recordLabels,
			@RequestParam(value = "estado", required = false) final List<ConditionBucket> estadoParams,
			@RequestParam(value = "sort", required = false) final ProductSortOrder sortOrder,
			@RequestParam(value = "page", defaultValue = "1") final int page) {

		if (page < 1) {
			throw new IllegalArgumentException("Invalid page");
		}

		final ProductSearchCriteria criteria = productService.getProductSearchCriteria(
				searchText,
				categoryIds,
				minPrice,
				maxPrice,
				recordLabels,
				estadoParams,
				sortOrder,
				page);

		final PaginatedResult<Product> productsPage = productService.listProducts(criteria);

		final Map<Long, String> productImageUrls = productImageUrlsFor(productsPage.getResults());
		final Map<Long, SellerRatingSummary> sellerRatingByUserId = sellerRatingsFor(productsPage.getResults());

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

		final ProductSortOrder effectiveSortOrder = sortOrder != null ? sortOrder : ProductSortOrder.NEWEST;

		final List<ConditionBucket> buckets = new ArrayList<>();
		if (estadoParams != null) {
			for (ConditionBucket bucket : estadoParams) {
				if (bucket != null) {
					buckets.add(bucket);
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
		mav.addObject("selectedCategoryIds", selectedCategoryIds);
		mav.addObject("selectedLabels", selectedLabels);
		mav.addObject("selectedEstados", selectedEstados);
		mav.addObject("filterMinPrice", minPrice != null ? minPrice.toPlainString() : "");
		mav.addObject("filterMaxPrice", maxPrice != null ? maxPrice.toPlainString() : "");
		mav.addObject("sortOptions", ProductSortOrder.values());
		mav.addObject("selectedSort", effectiveSortOrder.name());
		mav.addObject("activeSearchText", hasActiveSearch ? trimmedSearch : null);
		mav.addObject("hasActiveFilters", hasActiveFilters);
		mav.addObject("noProductsMatchFilters", productsPage.getResults().isEmpty() && hasActiveFilters);
		return mav;
	}

	@RequestMapping(value = "/for-you", method = RequestMethod.GET)
	public ModelAndView forYou(
			@AuthenticationPrincipal PawAuthUser authUser,
			@RequestParam(value = "page", defaultValue = "1") final int page) {

		if (authUser == null) {
			return new ModelAndView("redirect:/login");
		}

		if (page < 1) {
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

			productImageUrls.putAll(productImageUrlsFor(productsPage.getResults()));
			sellerRatingByUserId = sellerRatingsFor(productsPage.getResults());
		}

		mav.addObject("productsPage", productsPage);
		mav.addObject("products", productsPage.getResults());
		mav.addObject("productImageUrls", productImageUrls);
		mav.addObject("sellerRatingByUserId", sellerRatingByUserId);

		final List<Product> wishlistProducts = productService.getRecommendedProducts(currentUserId, 12, null);
		final Map<Long, String> wishlistProductImageUrls = productImageUrlsFor(wishlistProducts);
		final Map<Long, SellerRatingSummary> wishlistSellerRatingByUserId = sellerRatingsFor(wishlistProducts);
		mav.addObject("wishlistProducts", wishlistProducts);
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

		final PaginatedResult<User> usersPage;
		if (hasQuery) {
			usersPage = userService.searchActiveSellers(query.trim(), page, COMMUNITY_USER_PAGE_SIZE);
			mav.addObject("showingSearchResults", true);
		} else {
			usersPage = userService.getFeaturedActiveSellers(page, COMMUNITY_USER_PAGE_SIZE);
			mav.addObject("showingSearchResults", false);
		}

		final List<User> displayedUsers = usersPage.getResults();
		final List<Long> displayedUserIds = new ArrayList<>();
		for (User user : displayedUsers) {
			displayedUserIds.add(user.getId());
		}

		final Map<Long, Long> userFollowerCounts = userService.countFollowersByUserIds(displayedUserIds);
		final Map<Long, Long> userPublicationCounts = productService.countActiveProductsByUserIds(displayedUserIds);
		final Map<Long, List<Product>> productsByUserId =
				productService.listLatestActiveProductsByUserIds(displayedUserIds, COMMUNITY_PRODUCT_LIMIT);

		final Map<Long, PaginatedResult<Product>> productPagesByUserId = new HashMap<>();
		final List<Long> initialProductIds = new ArrayList<>();
		for (User user : displayedUsers) {
			final List<Product> userProducts = productsByUserId.getOrDefault(user.getId(), Collections.emptyList());
			for (Product product : userProducts) {
				initialProductIds.add(product.getId());
			}
			productPagesByUserId.put(
					user.getId(),
					new PaginatedResult<>(
							userProducts,
							1,
							COMMUNITY_PRODUCT_LIMIT,
							userPublicationCounts.getOrDefault(user.getId(), 0L)
					)
			);
		}

		final Set<Long> productIdsWithImages = imageService.findProductIdsWithImages(initialProductIds);
		final Map<Long, String> productImageUrls = new HashMap<>();
		for (Long productId : productIdsWithImages) {
			productImageUrls.put(productId, "/images/product/" + productId);
		}

		mav.addObject("usersPage", usersPage);
		mav.addObject("users", displayedUsers);
		mav.addObject("userFollowerCounts", userFollowerCounts);
		mav.addObject("userPublicationCounts", userPublicationCounts);
		mav.addObject("sellerProductPagesByUserId", productPagesByUserId);
		mav.addObject("productImageUrls", productImageUrls);

		if (authUser != null) {
			final Map<Long, Boolean> followStatusMap =
					userService.followingStatusByUserIds(authUser.getUser().getId(), displayedUserIds);
			mav.addObject("followStatusMap", followStatusMap);
			mav.addObject("currentUserId", authUser.getUser().getId());
		}

		return mav;
	}

	private Map<Long, String> productImageUrlsFor(final List<Product> products) {
		final Map<Long, String> productImageUrls = new HashMap<>();
		if (products == null || products.isEmpty()) {
			return productImageUrls;
		}

		final List<Long> productIds = new ArrayList<>();
		for (Product product : products) {
			productIds.add(product.getId());
		}
		final Set<Long> productIdsWithImages = imageService.findProductIdsWithImages(productIds);
		for (Long productId : productIdsWithImages) {
			productImageUrls.put(productId, "/images/product/" + productId);
		}
		return productImageUrls;
	}

	private Map<Long, SellerRatingSummary> sellerRatingsFor(final List<Product> products) {
		final Set<Long> distinctSellerIds = new HashSet<>();
		if (products != null) {
			for (Product product : products) {
				distinctSellerIds.add(product.getUserId());
			}
		}
		return reviewService.sellerRatingByUserId(distinctSellerIds);
	}
}
