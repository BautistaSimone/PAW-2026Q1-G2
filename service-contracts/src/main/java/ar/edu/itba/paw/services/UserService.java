package ar.edu.itba.paw.services;

import java.util.Optional;

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

    Optional<User> findByEmail(final String email);
    Optional<User> findById(final Long id);

    Boolean isPasswordEmpty(User usr);

	void enable(final Long id);

}
