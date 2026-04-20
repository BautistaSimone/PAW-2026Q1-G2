package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Date;
import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.PasswordToken;

@Repository
public class PasswordTokenJdbcDao implements PasswordTokenDao {

    private static final RowMapper<PasswordToken> PASSWORD_TOKEN_ROW_MAPPER = (rs, rowNum) ->
        new PasswordToken(
            rs.getLong("token_id"),
            rs.getLong("user_id"),
            rs.getString("token"),
            rs.getDate("expiration_date")
        );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public PasswordTokenJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("password_tokens").usingGeneratedKeyColumns("token_id");
    }

    @Override
    public PasswordToken createToken(final Long userId, final String token, final Date expirationDate) {

        final Map<String, Object> values = new HashMap<>();
        values.put("user_id", userId);
        values.put("token", token);
        values.put("expiration_date", expirationDate);

        // Delete previous token, we don't want more than one per user
        jdbcTemplate.update("DELETE FROM password_tokens WHERE user_id = ?", userId);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new PasswordToken(
            id.longValue(),
            userId,
            token,
            expirationDate
        );
    }

    @Override
    public Optional<PasswordToken> findByUserId(final Long userId) {
        return jdbcTemplate.query("SELECT * FROM password_tokens WHERE user_id = ?", PASSWORD_TOKEN_ROW_MAPPER, userId).stream().findAny();
    }

    @Override
    public Optional<PasswordToken> findByToken(final String token) {
        return jdbcTemplate.query("SELECT * FROM password_tokens WHERE token = ?", PASSWORD_TOKEN_ROW_MAPPER, token).stream().findAny();
    }

}
