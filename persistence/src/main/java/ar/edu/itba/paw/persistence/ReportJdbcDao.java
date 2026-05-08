package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.itba.paw.models.PaginatedResult;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;

@Repository
public class ReportJdbcDao implements ReportDao {

    private static final RowMapper<Report> REPORT_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        final Timestamp ts = rs.getTimestamp("created_at");
        return new Report(
            rs.getLong("report_id"),
            rs.getLong("product_id"),
            rs.getLong("owner_user_id"),
            rs.getLong("reporter_user_id"),
            ts != null ? ts.toLocalDateTime() : null
        );
    };

    private static final RowMapper<ReportedProduct> REPORTED_PRODUCT_ROW_MAPPER = (ResultSet rs, int rowNum) ->
        new ReportedProduct(
            rs.getLong("product_id"),
            rs.getLong("owner_user_id"),
            rs.getInt("report_count"),
            rs.getString("product_title"),
            rs.getString("product_artist"),
            rs.getString("owner_username")
        );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public ReportJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reports")
            .usingGeneratedKeyColumns("report_id")
            .usingColumns("product_id", "owner_user_id", "reporter_user_id");
    }

    @Override
    public Report create(final long productId, final long ownerUserId, final long reporterUserId) {
        final Map<String, Object> values = new HashMap<>();
        values.put("product_id", productId);
        values.put("owner_user_id", ownerUserId);
        values.put("reporter_user_id", reporterUserId);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new Report(id.longValue(), productId, ownerUserId, reporterUserId, null);
    }

    @Override
    public boolean existsByProductAndReporter(final long productId, final long reporterUserId) {
        final Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reports WHERE product_id = ? AND reporter_user_id = ?",
            Integer.class, productId, reporterUserId
        );
        return count != null && count > 0;
    }

    @Override
    public PaginatedResult<ReportedProduct> findAllGroupedByProduct(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        final Integer totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT r.product_id) FROM reports r", Integer.class
        );

        int total = totalCount != null ? totalCount : 0;
        int maxPage = (int) Math.ceil((double) total / pageSize);

        List<ReportedProduct> results = jdbcTemplate.query(
            "SELECT r.product_id, r.owner_user_id, COUNT(*) AS report_count, "
                + "p.title AS product_title, p.artist AS product_artist, "
                + "u.username AS owner_username "
                + "FROM reports r "
                + "JOIN products p ON r.product_id = p.product_id "
                + "JOIN users u ON r.owner_user_id = u.user_id "
                + "GROUP BY r.product_id, r.owner_user_id, p.title, p.artist, u.username "
                + "ORDER BY report_count DESC "
                + "OFFSET ? LIMIT ?",
            REPORTED_PRODUCT_ROW_MAPPER,
            offset, pageSize
        );

        return new PaginatedResult<>(results, page, maxPage, total);
    }

    @Override
    public void deleteByProductId(final long productId) {
        jdbcTemplate.update("DELETE FROM reports WHERE product_id = ?", productId);
    }
}
