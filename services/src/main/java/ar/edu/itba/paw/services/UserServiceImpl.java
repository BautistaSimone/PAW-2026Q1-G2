package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductSearchCriteria;

import ar.edu.itba.paw.persistence.UserDao;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReportService reportService;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    private static String trimToNull(final String s) {
        if (s == null) {
            return null;
        }
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    @Transactional
    public User createUser(
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
            final String cbuCvu) {

        final String encodedPassword = passwordEncoder.encode(password);

        LOGGER.atDebug().addArgument(email).log("About to attempt register user {}");

        return userDao.createUser(
                trimToNull(email),
                encodedPassword,
                trimToNull(username),
                mod,
                enabled,
                trimToNull(firstName),
                trimToNull(lastName),
                trimToNull(streetName),
                trimToNull(streetNumber),
                trimToNull(neighborhood),
                trimToNull(province),
                trimToNull(extraAddressInfo),
                trimToNull(cbuCvu));
    }

    @Override
    @Transactional
    public void updateUserProfile(
            final Long userId,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu) {
        userDao.updateUserProfile(
                userId,
                trimToNull(firstName),
                trimToNull(lastName),
                trimToNull(streetName),
                trimToNull(streetNumber),
                trimToNull(neighborhood),
                trimToNull(province),
                trimToNull(extraAddressInfo),
                trimToNull(cbuCvu));
    }

    @Override
    @Transactional
    public void updateFavoriteCategories(final Long userId, final List<Long> categoryIds) {
        userDao.updateFavoriteCategories(userId, categoryIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }

    @Override
	@Transactional
	public void updatePassword(final Long userId, final String newPassword) {
		// Encode password before storing
		final String encodedPassword = passwordEncoder.encode(newPassword);

		userDao.updatePassword(userId, encodedPassword);
	}

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(final Long id) {
        return userDao.findById(id);
    }

	@Override
    @Transactional(readOnly = true)
    public Boolean isPasswordEmpty(User usr) {
        return passwordEncoder.matches("", usr.getPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isVerified(User usr) {
        return usr.getEnabled();
    }

	@Override
    @Transactional
    public void enable(final Long id) {
        userDao.enable(id);
    }

	@Override
    @Transactional
    public void ban(final Long id) {

        // Hide all their active products
        final ProductSearchCriteria criteria = new ProductSearchCriteria(
            null, Collections.emptyList(), null, null,
            Collections.emptyList(), Collections.emptyList(), null, id,
            1, 1000000
        );

        final List<Product> userProducts = productService.listProducts(criteria).getResults();
        for (Product p : userProducts) {
            productService.hideProductByAdmin(p.getId());
            reportService.deleteByProductId(p.getId());
        }

        userDao.ban(id);
    }

	@Override
    @Transactional
	public void toggleWishlistProduct(final Long userId, final Long productId) {
        Product product = productService.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        if (this.isProductInWishlist(userId, productId)) {
            userDao.removeWishlistProduct(userId, product);
        } else {
            userDao.addWishlistProduct(userId, product);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isProductInWishlist(final Long userId, final Long productId) {
        return userDao.isProductInWishlist(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getWishlistProducts(final Long userId, final int limit) {
        return userDao.getWishlistProducts(userId, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getWishlistCategoryIds(final Long userId) {
        return userDao.getWishlistCategoryIds(userId);
    }

    @Override
    @Transactional
    public void follow(final Long followerId, final Long followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        userDao.follow(followerId, followedId);
    }

    @Override
    @Transactional
    public void unfollow(final Long followerId, final Long followedId) {
        userDao.unfollow(followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(final Long followerId, final Long followedId) {
        return userDao.isFollowing(followerId, followedId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowers(final Long userId) {
        return userDao.countFollowers(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowing(final Long userId) {
        return userDao.countFollowing(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> getFollowers(final Long userId, final int page, final int pageSize) {
        return userDao.getFollowers(userId, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> getFollowing(final Long userId, final int page, final int pageSize) {
        return userDao.getFollowing(userId, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getFollowedUserIds(final Long userId) {
        return userDao.getFollowedUserIds(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> searchUsers(final String query, final int page, final int pageSize) {
        return userDao.searchUsers(query, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getMostFollowedUsers(final int limit) {
        return userDao.getMostFollowedUsers(limit);
    }
}
