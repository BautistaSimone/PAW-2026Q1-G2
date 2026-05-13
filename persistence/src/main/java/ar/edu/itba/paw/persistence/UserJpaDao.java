package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.User;

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
}
