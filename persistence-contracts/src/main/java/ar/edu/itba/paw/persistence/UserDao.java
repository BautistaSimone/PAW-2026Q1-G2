package ar.edu.itba.paw.persistence;

import java.util.Optional;
import java.util.Set;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;

public interface UserDao {

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

    void updatePassword(final Long userId, final String encodedPassword);
    Optional<User> findByEmail(final String email);

    Optional<User> findById(final Long id);

	void enable(final Long id);
	void ban(final Long id);

	void addWishlistProduct(final Long id, Product product);
	Boolean isProductInWishlist(final Long userId, final Long productId);
}
