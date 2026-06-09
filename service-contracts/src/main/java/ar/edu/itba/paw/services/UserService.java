package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.UserSortOrder;

public interface UserService {

    User createUser(
            final String email,
            final String password,
            final String username,
            final boolean mod,
            final boolean enabled,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu);

    Optional<User> createUserIfEmailAvailable(
            final String email,
            final String password,
            final String username,
            final Boolean mod,
            final Boolean enabled,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu);

    void updateUserProfile(
            final Long userId,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu,
            final String language);

    void updatePassword(final Long userId, final String newPassword);

    void updateFavoriteCategories(final Long userId, final List<Long> categoryIds);

    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);
        List<User> findByIds(final List<Long> ids);

    boolean isPasswordEmpty(User usr);
    boolean isVerified(User usr);
    boolean isAdmin(final Long userId);
    boolean hasCompleteBuyerDataForPurchase(final Long userId);

	void enable(final Long id);

	void ban(final Long id);

	void toggleWishlistProduct(final Long userId, final Long productId);

	boolean isProductInWishlist(final Long userId, final Long productId);
        PaginatedResult<Product> getWishlistProducts(final Long userId, final int page, final int pageSize);
        List<Long> getWishlistCategoryIds(final Long userId);

	void follow(final Long followerId, final Long followedId);
	void unfollow(final Long followerId, final Long followedId);
	void toggleFollow(final Long followerId, final Long followedId);
	boolean isFollowing(final Long followerId, final Long followedId);
	long countFollowers(final Long userId);
	long countFollowing(final Long userId);
	PaginatedResult<User> getFollowers(final Long userId, final int page, final int pageSize);
	PaginatedResult<User> getFollowing(final Long userId, final int page, final int pageSize);
	PaginatedResult<User> searchFollowers(final Long userId, final String query, final int page, final int pageSize);
	PaginatedResult<User> searchFollowing(final Long userId, final String query, final int page, final int pageSize);
	List<Long> getFollowedUserIds(final Long userId);
	PaginatedResult<User> searchUsers(final String query, final int page, final int pageSize);
	List<User> getMostFollowedUsers(final int limit);
	PaginatedResult<User> getFeaturedActiveSellers(final int page, final int pageSize);
	PaginatedResult<User> getFeaturedActiveSellers(final int page, final int pageSize, final UserSortOrder sortOrder);
	PaginatedResult<User> searchActiveSellers(final String query, final int page, final int pageSize);
	PaginatedResult<User> searchActiveSellers(final String query, final int page, final int pageSize, final UserSortOrder sortOrder);
	Map<Long, Long> countFollowersByUserIds(final List<Long> userIds);
	Map<Long, Boolean> followingStatusByUserIds(final Long followerId, final List<Long> followedIds);

	/** Whether {@code currentUserId} follows each distinct user across the given follower/following lists. */
	Map<Long, Boolean> followStatusForUsers(final Long currentUserId, final List<User> followers, final List<User> following);
}
