package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.PasswordToken;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.UserSortOrder;

import ar.edu.itba.paw.persistence.PasswordTokenDao;
import ar.edu.itba.paw.persistence.UserDao;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;
    private final NotificationService notificationService;
    private final PasswordTokenDao passwordTokenDao;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final PasswordEncoder passwordEncoder,
            @Lazy final ProductService productService,
            final NotificationService notificationService,
            final PasswordTokenDao passwordTokenDao) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
        this.notificationService = notificationService;
        this.passwordTokenDao = passwordTokenDao;
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
            final boolean mod,
            final boolean enabled,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu) {

        final String normalizedEmail = trimToNull(email);
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        return createUserUnchecked(
                normalizedEmail,
                password,
                username,
                mod,
                enabled,
                firstName,
                lastName,
                streetName,
                streetNumber,
                neighborhood,
                province,
                extraAddressInfo,
                cbuCvu);
    }

    @Override
    @Transactional
    public Optional<User> createUserIfEmailAvailable(
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
        final String normalizedEmail = trimToNull(email);
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            return Optional.empty();
        }
        try {
            return Optional.of(createUserUnchecked(
                    normalizedEmail,
                    password,
                    username,
                    mod,
                    enabled,
                    firstName,
                    lastName,
                    streetName,
                    streetNumber,
                    neighborhood,
                    province,
                    extraAddressInfo,
                    cbuCvu));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    private User createUserUnchecked(
            final String normalizedEmail,
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

        LOGGER.atDebug().addArgument(normalizedEmail).log("About to attempt register user {}");

        return userDao.createUser(
                normalizedEmail,
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
            final String cbuCvu,
            final String language) {
        userDao.updateUserProfile(
                userId,
                trimToNull(firstName),
                trimToNull(lastName),
                trimToNull(streetName),
                trimToNull(streetNumber),
                trimToNull(neighborhood),
                trimToNull(province),
                trimToNull(extraAddressInfo),
                trimToNull(cbuCvu),
                trimToNull(language));
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
    @Transactional
    public boolean resetPasswordWithToken(final String token, final String newPassword) {
        final Optional<PasswordToken> passTokenOpt = passwordTokenDao.findByToken(token);
        if (!passTokenOpt.isPresent()) {
            return false;
        }

        final PasswordToken passToken = passTokenOpt.get();
        if (passToken.getExpirationDate().isBefore(Instant.now())) {
            return false;
        }

        updatePassword(passToken.getUserId(), newPassword);
        passwordTokenDao.deleteByToken(token);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(final Long id) {
        return userDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByIds(final List<Long> ids) {
        return userDao.findByIds(ids);
    }

	@Override
    @Transactional(readOnly = true)
    public boolean isPasswordEmpty(User usr) {
        return passwordEncoder.matches("", usr.getPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVerified(User usr) {
        return usr.getEnabled();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(final Long userId) {
        return userDao.findById(userId)
                .map(User::getMod)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCompleteBuyerDataForPurchase(final Long userId) {
        return userDao.findById(userId)
                .map(User::hasCompleteBuyerDataForPurchase)
                .orElse(false);
    }

	@Override
    @Transactional
    public void enable(final Long id) {
        userDao.enable(id);
    }

	@Override
    @Transactional
    public void ban(final Long id) {
        productService.hideAllProductsByAdmin(id);
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
    public boolean isProductInWishlist(final Long userId, final Long productId) {
        return userDao.isProductInWishlist(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> getWishlistProducts(final Long userId, final int page, final int pageSize) {
        return userDao.getWishlistProducts(userId, page, pageSize);
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
        notificationService.notifyFollow(followedId, followerId);
    }

    @Override
    @Transactional
    public void unfollow(final Long followerId, final Long followedId) {
        userDao.unfollow(followerId, followedId);
    }

    @Override
    @Transactional
    public void toggleFollow(final Long followerId, final Long followedId) {
        if (followerId.equals(followedId)) {
            return;
        }
        if (userDao.isFollowing(followerId, followedId)) {
            userDao.unfollow(followerId, followedId);
            return;
        }
        userDao.follow(followerId, followedId);
        notificationService.notifyFollow(followedId, followerId);
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
    public PaginatedResult<User> searchFollowers(final Long userId, final String query, final int page, final int pageSize) {
        return userDao.searchFollowers(userId, query, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> searchFollowing(final Long userId, final String query, final int page, final int pageSize) {
        return userDao.searchFollowing(userId, query, page, pageSize);
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

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> getFeaturedActiveSellers(final int page, final int pageSize) {
        return userDao.getFeaturedActiveSellers(page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> getFeaturedActiveSellers(final int page, final int pageSize, final UserSortOrder sortOrder) {
        return userDao.getFeaturedActiveSellers(page, pageSize, sortOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> searchActiveSellers(final String query, final int page, final int pageSize) {
        return userDao.searchActiveSellers(query, page, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<User> searchActiveSellers(final String query, final int page, final int pageSize, final UserSortOrder sortOrder) {
        return userDao.searchActiveSellers(query, page, pageSize, sortOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countFollowersByUserIds(final List<Long> userIds) {
        return userDao.countFollowersByUserIds(userIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> followingStatusByUserIds(final Long followerId, final List<Long> followedIds) {
        return userDao.followingStatusByUserIds(followerId, followedIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> followStatusForUsers(
            final Long currentUserId,
            final List<User> followers,
            final List<User> following) {
        final List<Long> userIds = new ArrayList<>();
        final Set<Long> seenIds = new HashSet<>();
        if (followers != null) {
            for (User user : followers) {
                if (seenIds.add(user.getId())) {
                    userIds.add(user.getId());
                }
            }
        }
        if (following != null) {
            for (User user : following) {
                if (seenIds.add(user.getId())) {
                    userIds.add(user.getId());
                }
            }
        }
        return followingStatusByUserIds(currentUserId, userIds);
    }
}
