package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.EntityNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;

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
            .orElseThrow(() -> new EntityNotFoundException("User not found"));;

        // Set it, hibernate will take care of it
        user.getWishlistProducts().add(product);
    }

    @Override
	public Boolean isProductInWishlist(final Long userId, final Long productId) {
        final TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(p) " +
            "FROM User u JOIN u.wishlistProducts p " +
            "WHERE u.id = :userId " +
            "AND p.id = :productId",
            Long.class
        );

        query.setParameter("userId", userId);
        query.setParameter("productId", productId);

        return query.getSingleResult() > 0;
    }

    @Override
    public List<Product> getWishlistProducts(final Long userId, final int limit) {
        final int safeLimit = Math.max(limit, 1);
        return em.createQuery(
                "SELECT p FROM User u JOIN u.wishlistProducts p "
                        + "WHERE u.id = :userId ORDER BY p.published DESC",
                Product.class)
            .setParameter("userId", userId)
            .setMaxResults(safeLimit)
            .getResultList();
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
        em.createNativeQuery("INSERT INTO user_follows (follower_id, followed_id) VALUES (:fid, :lid) ON CONFLICT DO NOTHING")
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

        final String likePattern = "%" + query.trim().toLowerCase() + "%";

        final long totalCount = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE LOWER(u.username) LIKE :q AND u.banned = false", Long.class)
            .setParameter("q", likePattern)
            .getSingleResult();

        if (totalCount == 0) {
            return new PaginatedResult<>(Collections.emptyList(), safePage, safeSize, 0);
        }

        final List<User> users = em.createQuery(
                "FROM User u WHERE LOWER(u.username) LIKE :q AND u.banned = false ORDER BY u.username ASC", User.class)
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
}
