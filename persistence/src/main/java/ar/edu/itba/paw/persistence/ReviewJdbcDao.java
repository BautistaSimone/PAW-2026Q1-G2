package ar.edu.itba.paw.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.SellerRatingSummary;

@Repository
public class ReviewJdbcDao implements ReviewDao {

    private static final RowMapper<Review> REVIEW_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        String buyerUsername = null;
        try {
            buyerUsername = rs.getString("buyer_username");
        } catch (SQLException ignored) {
        }
        Timestamp ts = rs.getTimestamp("created_at");
        return new Review(
            rs.getLong("review_id"),
            rs.getLong("purchase_id"),
            rs.getLong("seller_id"),
            rs.getLong("buyer_id"),
            rs.getInt("score"),
            rs.getString("review"),
            ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
            buyerUsername
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public ReviewJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reviews")
            .usingGeneratedKeyColumns("review_id");
    }

    @Override
    public Review create(long purchaseId, long sellerId, long buyerId, int score, String text) {
        final Map<String, Object> values = new HashMap<>();
        values.put("purchase_id", purchaseId);
        values.put("seller_id", sellerId);
        values.put("buyer_id", buyerId);
        values.put("score", score);
        values.put("review", text);
        values.put("created_at", Timestamp.valueOf(LocalDateTime.now()));

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new Review(id.longValue(), purchaseId, sellerId, buyerId, score, text, LocalDateTime.now(), null);
    }

    @Override
    public Optional<Review> findByPurchaseId(long purchaseId) {
        return jdbcTemplate.query(
            "SELECT r.*, u.username AS buyer_username FROM reviews r " +
            "JOIN users u ON r.buyer_id = u.user_id " +
            "WHERE r.purchase_id = ?",
            REVIEW_ROW_MAPPER, purchaseId
        ).stream().findAny();
    }

    @Override
    public List<Review> findBySellerId(long sellerId) {
        return jdbcTemplate.query(
            "SELECT r.*, u.username AS buyer_username FROM reviews r " +
            "JOIN users u ON r.buyer_id = u.user_id " +
            "WHERE r.seller_id = ? ORDER BY r.created_at DESC",
            REVIEW_ROW_MAPPER, sellerId
        );
    }

    @Override
    public SellerRatingSummary summaryForSeller(long sellerId) {
        return jdbcTemplate.queryForObject(
            "SELECT COALESCE(AVG(score), 0) AS avg_score, COUNT(*) AS review_count FROM reviews WHERE seller_id = ?",
            (ResultSet rs, int rowNum) -> new SellerRatingSummary(rs.getDouble("avg_score"), rs.getInt("review_count")),
            sellerId
        );
    }
}
