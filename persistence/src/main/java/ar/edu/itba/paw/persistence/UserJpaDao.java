package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSet;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

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
        
        final User user =  new User(
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
        
        em.createQuery(
            "UPDATE User SET firstName = :firstName, lastName = :last_name, streetName = :street_name, "
            + "streetNumber = :street_number, neighborhood = :neighborhood, province = :province, "
            + "extraAddressInfo = :extra_address_info, cbuCvu = :cbu_cvu WHERE userId = :user_id")
            .setParameter("first_name", firstName)
            .setParameter("last_name", lastName)
            .setParameter("street_name", streetName)
            .setParameter("street_number", streetNumber)
            .setParameter("neightborhood", neighborhood)
            .setParameter("province", province)
            .setParameter("extra_address_info", extraAddressInfo)
            .setParameter("cbu_cvu", cbuCvu)
            .setParameter("user_id", userId)
            .executeUpdate();
    }

    @Override
    public void updatePassword(final Long userId, final String encodedPassword) {
         em.createQuery(
            "UPDATE User SET password = :password WHERE userId = :user_id")
            .setParameter("password", encodedPassword)
            .setParameter("user_id", userId)
            .executeUpdate();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        final TypedQuery<User> query = em.createQuery("FROM User WHERE email = :email", User.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

	@Override
    public void enable(final Long id) {
        em.createQuery(
            "UPDATE User SET enabled = true WHERE userId = :user_id")
            .setParameter("user_id", id)
            .executeUpdate();
    }

    @Override
    public void ban(final Long id) {
        em.createQuery(
            "UPDATE User SET banned = true WHERE userId = :user_id")
            .setParameter("user_id", id)
            .executeUpdate();
    }
}