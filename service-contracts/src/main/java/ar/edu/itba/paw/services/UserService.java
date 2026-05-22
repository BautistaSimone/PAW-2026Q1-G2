package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

public interface UserService {

    User createUser(
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
            final String cbuCvu);

    void updatePassword(final Long userId, final String newPassword);

    void updateFavoriteCategories(final Long userId, final List<Long> categoryIds);

    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);

    Boolean isPasswordEmpty(User usr);
    Boolean isVerified(User usr);

	void enable(final Long id);

	void ban(final Long id);

	void toggleWishlistProduct(final Long userId, final Long productId);

	Boolean isProductInWishlist(final Long userId, final Long productId);
        List<Product> getWishlistProducts(final Long userId, final int limit);
        List<Long> getWishlistCategoryIds(final Long userId);

	void follow(final Long followerId, final Long followedId);
	void unfollow(final Long followerId, final Long followedId);
	boolean isFollowing(final Long followerId, final Long followedId);
	long countFollowers(final Long userId);
	long countFollowing(final Long userId);
	PaginatedResult<User> getFollowers(final Long userId, final int page, final int pageSize);
	PaginatedResult<User> getFollowing(final Long userId, final int page, final int pageSize);
	List<Long> getFollowedUserIds(final Long userId);
	PaginatedResult<User> searchUsers(final String query, final int page, final int pageSize);
	List<User> getMostFollowedUsers(final int limit);
}
