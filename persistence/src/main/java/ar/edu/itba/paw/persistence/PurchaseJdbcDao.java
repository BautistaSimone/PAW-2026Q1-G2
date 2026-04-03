package ar.edu.itba.paw.persistence;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

@Repository
public class PurchaseJdbcDao implements PurchaseDao {

    private final static RowMapper<Purchase> PURCHASE_ROW_MAPPER = (ResultSet rs, int rowNum) ->
        new Purchase(
            rs.getLong("purchase_id"),
            rs.getLong("product_id"),
            rs.getLong("user_id"),
            rs.getDate("date").toLocalDate(),
            PurchaseStatus.valueOf(rs.getString("status")),
            rs.getString("buyer_token"),
            rs.getString("seller_token"));

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public PurchaseJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("purchases")
            .usingGeneratedKeyColumns("purchase_id");
    }

    @Override
    public Purchase createPurchase(Long productId, Long buyerId, PurchaseStatus status, String buyerToken, String sellerToken) {
        final Map<String, Object> values = new HashMap<>();
        values.put("product_id", productId);
        values.put("user_id", buyerId);
        values.put("date", java.sql.Date.valueOf(LocalDate.now()));
        values.put("status", status.name());
        values.put("buyer_token", buyerToken);
        values.put("seller_token", sellerToken);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new Purchase(id.longValue(), productId, buyerId, LocalDate.now(), status, buyerToken, sellerToken);
    }

    @Override
    public Optional<Purchase> findById(Long purchaseId) {
        return jdbcTemplate.query("SELECT * FROM purchases WHERE purchase_id = ?", PURCHASE_ROW_MAPPER, purchaseId)
            .stream().findAny();
    }

    @Override
    public void updateStatus(Long purchaseId, PurchaseStatus status) {
        jdbcTemplate.update("UPDATE purchases SET status = ? WHERE purchase_id = ?", status.name(), purchaseId);
    }
}
