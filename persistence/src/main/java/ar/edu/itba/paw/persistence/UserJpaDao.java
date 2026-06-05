package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.EntityNotFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.ProductState;

@Repository
public class UserJpaDao implements UserDao {

    @PersistenceContext
    private EntityManager em;

    @Override
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

        final User user = new User(
                email,
                password,
                username,
                mod,
                enabled,
                false,
                firstName,
                lastName,
                streetName,
                streetNumber,
                neighborhood,
                province,
                extraAddressInfo,
                cbuCvu);

        em.persist(user);

        return user;
    }

    @Override
    public Optional<User> findById(final Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public List<User> findByIds(final List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return em.createQuery("FROM User u WHERE u.id IN :ids", User.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
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

        final User user = em.find(User.class, userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setStreetName(streetName);
        user.setStreetNumber(streetNumber);
        user.setNeighborhood(neighborhood);
        user.setProvince(province);
        user.setExtraAddressInfo(extraAddressInfo);
        user.setCbuCvu(cbuCvu);
    }

    @Override
    public void updatePassword(final Long userId, final String encodedPassword) {
        final User user = em.find(User.class, userId);
        user.setPassword(encodedPassword);
    }

    @Override
    public void updateFavoriteCategories(final Long userId, final List<Long> categoryIds) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }
        user.getFavoriteCategories().clear();
        if (categoryIds != null) {
            for (Long cid : categoryIds) {
                ar.edu.itba.paw.models.Category c = em.find(ar.edu.itba.paw.models.Category.class, cid);
                if (c != null) {
                    user.getFavoriteCategories().add(c);
                }
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        final TypedQuery<User> query = em.createQuery("FROM User WHERE email = :email", User.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public void enable(final Long id) {
        final User user = em.find(User.class, id);
        user.setEnabled(true);
    }

    @Override
    public void ban(final Long id) {
        final User user = em.find(User.class, id);
        user.setBanned(true);
    }

    @Override
    public void addWishlistProduct(final Long id, Product product) {
        User user = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        ;

        // Set it, hibernate will take care of it
        user.getWishlistProducts().add(product);
    }

    @Override
    public void removeWishlistProduct(final Long id, Product product) {
        User user = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        ;

        // Set it, hibernate will take care of it
        user.getWishlistProducts().remove(product);
    }

    @Override
    public boolean isProductInWishlist(final Long userId, final Long productId) {
        final TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(p) " +
                        "FROM User u JOIN u.wishlistProducts p " +
                        "WHERE u.id = :userId " +
                        "AND p.id = :productId",
                Long.class);

        query.setParameter("userId", userId);
        query.setParameter("productId", productId);

        return query.getSingleResult() > 0;
    }

    @Override
    public List<Product> getWishlistProducts(final Long userId, final int limit) {
        final int safeLimit = Math.max(limit, 1);

        // Paginate with 1 + 1 queries
        @SuppressWarnings("unchecked")
        List<Number> ids = em.createNativeQuery("SELECT product_id FROM user_wishlist_products WHERE user_id = :userId")
                .setParameter("userId", userId)
                .setFirstResult(0)
                .setMaxResults(safeLimit)
                .getResultList();

        if (ids.isEmpty()) {
            // return new PaginatedResult<>(Collections.emptyList(), 0, safeLimit, 0);
            return Collections.emptyList();
        }

        final TypedQuery<Product> selectQuery = em.createQuery("FROM Product WHERE productId IN :ids", Product.class)
                .setParameter("ids", ids.stream().map(Number::longValue).collect(Collectors.toList()));

        // FIXME: Allow page number to be specified
        // return new PaginatedResult<>(selectQuery.getResultList(), 0, safePageSize,
        // ids.size());
        return selectQuery.getResultList();
    }

    @Override
    public List<Long> getWishlistCategoryIds(final Long userId) {
        final List<Long> ids = em.createQuery(
                "SELECT DISTINCT c.id FROM User u JOIN u.wishlistProducts p "
                        + "JOIN p.categories c WHERE u.id = :userId",
                Long.class)
                .setParameter("userId", userId)
                .getResultList();
        return ids != null ? ids : Collections.emptyList();
    }

    @Override
    public void follow(final Long followerId, final Long followedId) {
        em.createNativeQuery(
                "INSERT INTO user_follows (follower_id, followed_id) VALUES (:fid, :lid) ON CONFLICT DO NOTHING")
                .setParameter("fid", followerId)
                .setParameter("lid", followedId)
                .executeUpdate();
    }

    @Override
    public void unfollow(final Long followerId, final Long followedId) {
        em.createNativeQuery("DELETE FROM user_follows WHERE follower_id = :fid AND followed_id = :lid")
                .setParameter("fid", followerId)
                .setParameter("lid", followedId)
                .executeUpdate();
    }

    @Override
    public boolean isFollowing(final Long followerId, final Long followedId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM user_follows WHERE follower_id = :fid AND followed_id = :lid")
                .setParameter("fid", followerId)
                .setParameter("lid", followedId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public long countFollowers(final Long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM user_follows WHERE followed_id = :uid")
                .setParameter("uid", userId)
                .getSingleResult();
        return count.longValue();
    }

    @Override
    public long countFollowing(final Long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM user_follows WHERE follower_id = :uid")
                .setParameter("uid", userId)
                .getSingleResult();
        return count.longValue();
    }

    @Override
    public PaginatedResult<User> getFollowers(final Long userId, final int page, final int pageSize) {
        final int safePage = Math.max(page, 1);
        final int safeSize = Math.max(pageSize, 1);

        final long totalCount = countFollowers(userId);
        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, 0);
        }

        @SuppressWarnings("unchecked")
        final List<Number> ids = em.createNativeQuery(
                "SELECT follower_id FROM user_follows WHERE followed_id = :uid ORDER BY created_at DESC")
                .setParameter("uid", userId)
                .setFirstResult((safePage - 1) * safeSize)
                .setMaxResults(safeSize)
                .getResultList();

        if (ids.isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, totalCount);
        }

        final List<Long> longIds = ids.stream().map(Number::longValue).collect(Collectors.toList());
        final List<User> users = em.createQuery("FROM User WHERE id IN :ids", User.class)
                .setParameter("ids", longIds)
                .getResultList();

        return new PaginatedResult<>(users, safePage, safeSize, totalCount);
    }

    @Override
    public PaginatedResult<User> getFollowing(final Long userId, final int page, final int pageSize) {
        final int safePage = Math.max(page, 1);
        final int safeSize = Math.max(pageSize, 1);

        final long totalCount = countFollowing(userId);
        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, 0);
        }

        @SuppressWarnings("unchecked")
        final List<Number> ids = em.createNativeQuery(
                "SELECT followed_id FROM user_follows WHERE follower_id = :uid ORDER BY created_at DESC")
                .setParameter("uid", userId)
                .setFirstResult((safePage - 1) * safeSize)
                .setMaxResults(safeSize)
                .getResultList();

        if (ids.isEmpty()) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, totalCount);
        }

        final List<Long> longIds = ids.stream().map(Number::longValue).collect(Collectors.toList());
        final List<User> users = em.createQuery("FROM User WHERE id IN :ids", User.class)
                .setParameter("ids", longIds)
                .getResultList();

        return new PaginatedResult<>(users, safePage, safeSize, totalCount);
    }

    @Override
    public List<Long> getFollowedUserIds(final Long userId) {
        @SuppressWarnings("unchecked")
        final List<Number> ids = em.createNativeQuery(
                "SELECT followed_id FROM user_follows WHERE follower_id = :uid")
                .setParameter("uid", userId)
                .getResultList();
        return ids.stream().map(Number::longValue).collect(Collectors.toList());
    }

    @Override
    public PaginatedResult<User> searchUsers(final String query, final int page, final int pageSize) {
        final int safePage = Math.max(page, 1);
        final int safeSize = Math.max(pageSize, 1);

        final String likePattern = "%" + escapeForLike(query.trim().toLowerCase()) + "%";

        final long totalCount = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE LOWER(u.username) LIKE :q ESCAPE '\\' AND u.banned = false",
                Long.class)
                .setParameter("q", likePattern)
                .getSingleResult();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, 0);
        }

        final List<User> users = em.createQuery(
                "FROM User u WHERE LOWER(u.username) LIKE :q ESCAPE '\\' AND u.banned = false ORDER BY u.username ASC",
                User.class)
                .setParameter("q", likePattern)
                .setFirstResult((safePage - 1) * safeSize)
                .setMaxResults(safeSize)
                .getResultList();

        return new PaginatedResult<>(users, safePage, safeSize, totalCount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> getMostFollowedUsers(final int limit) {
        final List<Number> ids = em.createNativeQuery(
                "SELECT u.user_id FROM users u " +
                        "LEFT JOIN user_follows uf ON u.user_id = uf.followed_id " +
                        "WHERE u.banned = false " +
                        "GROUP BY u.user_id " +
                        "ORDER BY COUNT(uf.follower_id) DESC, u.user_id ASC")
                .setMaxResults(limit)
                .getResultList();

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> longIds = ids.stream().map(Number::longValue).collect(Collectors.toList());
        return em.createQuery("FROM User WHERE id IN :ids", User.class)
                .setParameter("ids", longIds)
                .getResultList();
    }

    @Override
    public PaginatedResult<User> getFeaturedActiveSellers(final int page, final int pageSize) {
        final int safePage = Math.max(page, 1);
        final int safeSize = Math.max(pageSize, 1);

        final String activeState = ProductState.ACTIVE.getPersistenceValue();
        final Number countResult = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM users u " +
                        "WHERE u.banned = false " +
                        "AND EXISTS (" +
                        " SELECT 1 FROM products p " +
                        " WHERE p.user_id = u.user_id AND p.state = :state" +
                        ")")
                .setParameter("state", activeState)
                .getSingleResult();
        final long totalCount = countResult == null ? 0 : countResult.longValue();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, 0);
        }

        @SuppressWarnings("unchecked")
        final List<Number> ids = em.createNativeQuery(
                "SELECT u.user_id " +
                        "FROM users u " +
                        "JOIN products p ON p.user_id = u.user_id AND p.state = :state " +
                        "LEFT JOIN user_follows uf ON uf.followed_id = u.user_id " +
                        "WHERE u.banned = false " +
                        "GROUP BY u.user_id " +
                        "ORDER BY COUNT(DISTINCT uf.follower_id) DESC, " +
                        "COUNT(DISTINCT p.product_id) DESC, " +
                        "u.user_id ASC")
                .setParameter("state", activeState)
                .setFirstResult((safePage - 1) * safeSize)
                .setMaxResults(safeSize)
                .getResultList();

        final List<Long> orderedIds = ids.stream().map(Number::longValue).collect(Collectors.toList());
        return new PaginatedResult<>(findUsersPreservingOrder(orderedIds), safePage, safeSize, totalCount);
    }

    @Override
    public PaginatedResult<User> searchActiveSellers(final String query, final int page, final int pageSize) {
        final int safePage = Math.max(page, 1);
        final int safeSize = Math.max(pageSize, 1);
        final String rawQuery = query == null ? "" : query.trim().toLowerCase();
        final String likePattern = "%" + escapeForLike(rawQuery) + "%";
        final ProductState activeState = ProductState.ACTIVE;

        final long totalCount = em.createQuery(
                "SELECT COUNT(u) " +
                        "FROM User u " +
                        "WHERE LOWER(u.username) LIKE :q ESCAPE '\\' " +
                        "AND u.banned = false " +
                        "AND EXISTS (" +
                        " SELECT p.productId FROM Product p " +
                        " WHERE p.userId = u.id AND p.state = :state" +
                        ")",
                Long.class)
                .setParameter("q", likePattern)
                .setParameter("state", activeState)
                .getSingleResult();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, 0);
        }

        final List<User> users = em.createQuery(
                "FROM User u " +
                        "WHERE LOWER(u.username) LIKE :q ESCAPE '\\' " +
                        "AND u.banned = false " +
                        "AND EXISTS (" +
                        " SELECT p.productId FROM Product p " +
                        " WHERE p.userId = u.id AND p.state = :state" +
                        ") " +
                        "ORDER BY LOWER(u.username) ASC, u.id ASC",
                User.class)
                .setParameter("q", likePattern)
                .setParameter("state", activeState)
                .setFirstResult((safePage - 1) * safeSize)
                .setMaxResults(safeSize)
                .getResultList();

        return new PaginatedResult<>(users, safePage, safeSize, totalCount);
    }

    @Override
    public Map<Long, Long> countFollowersByUserIds(final List<Long> userIds) {
        final Map<Long, Long> counts = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return counts;
        }
        for (Long userId : userIds) {
            if (userId != null) {
                counts.put(userId, 0L);
            }
        }
        if (counts.isEmpty()) {
            return counts;
        }

        @SuppressWarnings("unchecked")
        final List<Object[]> rows = em.createNativeQuery(
                "SELECT followed_id, COUNT(*) " +
                        "FROM user_follows " +
                        "WHERE followed_id IN (:ids) " +
                        "GROUP BY followed_id")
                .setParameter("ids", counts.keySet())
                .getResultList();

        for (Object[] row : rows) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public Map<Long, Boolean> followingStatusByUserIds(final Long followerId, final List<Long> followedIds) {
        final Map<Long, Boolean> statuses = new HashMap<>();
        if (followerId == null || followedIds == null || followedIds.isEmpty()) {
            return statuses;
        }
        for (Long followedId : followedIds) {
            if (followedId != null) {
                statuses.put(followedId, false);
            }
        }
        if (statuses.isEmpty()) {
            return statuses;
        }

        @SuppressWarnings("unchecked")
        final List<Number> rows = em.createNativeQuery(
                "SELECT followed_id " +
                        "FROM user_follows " +
                        "WHERE follower_id = :followerId AND followed_id IN (:ids)")
                .setParameter("followerId", followerId)
                .setParameter("ids", statuses.keySet())
                .getResultList();

        for (Number followedId : rows) {
            statuses.put(followedId.longValue(), true);
        }
        return statuses;
    }

    private List<User> findUsersPreservingOrder(final List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<Long, Integer> orderById = new HashMap<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            orderById.put(orderedIds.get(i), i);
        }

        return em.createQuery("FROM User WHERE id IN :ids", User.class)
                .setParameter("ids", orderedIds)
                .getResultList()
                .stream()
                .sorted((left, right) -> Integer.compare(orderById.get(left.getId()), orderById.get(right.getId())))
                .collect(Collectors.toList());
    }

    private static String escapeForLike(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
