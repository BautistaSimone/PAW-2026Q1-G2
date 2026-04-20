package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.util.List;
import java.util.Optional;
import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;
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
            rs.getDate("expiration_date").toLocalDate()
        );

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PasswordTokenJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<PasswordToken> findByUserId(final Long userId) {
        return jdbcTemplate.query("SELECT * FROM password_tokens WHERE user_id = ?", PASSWORD_TOKEN_ROW_MAPPER, userId).stream().findAny();
    }
}
