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
            rs.getBoolean("mod"));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public UserJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("users").usingGeneratedKeyColumns("user_id");
    }

    @Override
    public User createUser(final String email, final String password, final String username, Boolean mod) {
        final Map<String, Object> values = new HashMap<>();
        values.put("email", email);
        values.put("password", password);
        values.put("username", username);
        values.put("mod", mod); 

        final Number id = jdbcInsert.executeAndReturnKey(values);
        
        return new User(id.longValue(), email, password, username, mod); // FIXME: Agregar mod al constructor
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
