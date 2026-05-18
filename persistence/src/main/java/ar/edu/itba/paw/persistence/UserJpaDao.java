package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.EntityNotFoundException;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

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
}
