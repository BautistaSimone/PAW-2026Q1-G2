package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.sql.ResultSet;
import java.sql.Array;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.itba.paw.models.User;

@Repository
public class UserJdbcDao implements UserDao {

    private static final RowMapper<Long> WISHLIST_ROW_MAPPER = (rs, rowNum) ->
        rs.getLong("product_id");

    private List<Long> findWishlistByUserId(final Long userId) {
        return jdbcTemplate.query(
            "SELECT * FROM wishlist_products " +
            "WHERE user_id = ?",
            WISHLIST_ROW_MAPPER, userId
        );
    }

    private User mapUser(
        final Long id,
        final String email,
        final String password,
        final String username,
        final Boolean mod,
        final Boolean enabled,
        final Boolean banned,
        final String firstName,
        final String lastName,
        final String streetName,
        final String streetNumber,
        final String neighborhood,
        final String province,
        final String extraAddressInfo,
        final String cbuCvu
    ) {
        final List<Long> wishlist = findWishlistByUserId(id);
        return new User(
                id,
                email,
                password,
                username,
                mod,
                enabled,
                banned,
                firstName,
                lastName,
                streetName,
                streetNumber,
                neighborhood,
                province,
                extraAddressInfo,
                cbuCvu,
                wishlist);
    }

    private User mapUserFromRow(final Map<String, Object> row) {
        return mapUser(
            ((Number) row.get("user_id")).longValue(),
            (String) row.get("email"),
            (String) row.get("password"),
            (String) row.get("username"),
            (Boolean) row.get("mod"),
            (Boolean) row.get("enabled"),
            (Boolean) row.get("banned"),
            (String) row.get("first_name"),
            (String) row.get("last_name"),
            (String) row.get("street_name"),
            (String) row.get("street_number"),
            (String) row.get("neighborhood"),
            (String) row.get("province"),
            (String) row.get("extra_address_info"),
            (String) row.get("cbu_cvu")
        );
    }

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public UserJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("users").usingGeneratedKeyColumns("user_id");
    }

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
        final Map<String, Object> values = new HashMap<>();
        values.put("email", email);
        values.put("password", password);
        values.put("username", username);
        values.put("mod", mod);
        values.put("enabled", enabled);
        values.put("first_name", firstName);
        values.put("last_name", lastName);
        values.put("street_name", streetName);
        values.put("street_number", streetNumber);
        values.put("neighborhood", neighborhood);
        values.put("province", province);
        values.put("extra_address_info", extraAddressInfo);
        values.put("cbu_cvu", cbuCvu);
        values.put("banned", false);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new User(
                id.longValue(),
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
                cbuCvu,
                Collections.emptyList());
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
        jdbcTemplate.update(
                "UPDATE users SET first_name = ?, last_name = ?, street_name = ?, street_number = ?, "
                        + "neighborhood = ?, province = ?, extra_address_info = ?, cbu_cvu = ? WHERE user_id = ?",
                firstName,
                lastName,
                streetName,
                streetNumber,
                neighborhood,
                province,
                extraAddressInfo,
                cbuCvu,
                userId);
    }

    @Override
    public void updatePassword(final Long userId, final String encodedPassword) {
        jdbcTemplate.update(
            "UPDATE users SET password = ? WHERE user_id = ?",
            encodedPassword,
            userId
        );
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        final List<Map<String, Object>> rows = 
            jdbcTemplate.queryForList("SELECT * FROM users WHERE email = ?", email);

        return rows.stream().findFirst().map(this::mapUserFromRow);
    }

    @Override
    public Optional<User> findById(final Long id) {
        final List<Map<String, Object>> rows = 
            jdbcTemplate.queryForList("SELECT * FROM users WHERE user_id = ?", id);

        return rows.stream().findFirst().map(this::mapUserFromRow);
    }

	@Override
    public void enable(final Long id) {
        jdbcTemplate.update(
            "UPDATE users SET enabled = true WHERE user_id = ?",
            id
        );
    }

    @Override
    public void ban(final Long id) {
        jdbcTemplate.update(
            "UPDATE users SET banned = true WHERE user_id = ?",
            id
        );
    }
}
