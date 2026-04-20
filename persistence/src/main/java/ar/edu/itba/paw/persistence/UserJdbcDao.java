package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.sql.ResultSet;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.itba.paw.models.User;

@Repository
public class UserJdbcDao implements UserDao {

    private final static RowMapper<User> USER_ROW_MAPPER = (ResultSet rs, int rowNum) ->
        new User(
            rs.getLong("user_id"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("username"),
            rs.getBoolean("mod"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("street_name"),
            rs.getString("street_number"),
            rs.getString("neighborhood"),
            rs.getString("province"),
            rs.getString("extra_address_info"),
            rs.getString("cbu_cvu"));

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
        values.put("first_name", firstName);
        values.put("last_name", lastName);
        values.put("street_name", streetName);
        values.put("street_number", streetNumber);
        values.put("neighborhood", neighborhood);
        values.put("province", province);
        values.put("extra_address_info", extraAddressInfo);
        values.put("cbu_cvu", cbuCvu);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new User(
                id.longValue(),
                email,
                password,
                username,
                mod,
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
        return jdbcTemplate.query("SELECT * FROM users WHERE email = ?", USER_ROW_MAPPER, email).stream().findAny();
    }

    @Override
    public Optional<User> findById(final Long id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE user_id = ?", USER_ROW_MAPPER, id).stream().findAny();
    }
}
