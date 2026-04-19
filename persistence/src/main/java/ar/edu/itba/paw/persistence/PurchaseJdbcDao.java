package ar.edu.itba.paw.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;

@Repository
public class PurchaseJdbcDao implements PurchaseDao {

    private static String encodePaymentMethod(final PurchaseStatus status, final String buyerToken, final String sellerToken) {
        return status.name() + "|" + buyerToken + "|" + sellerToken;
    }

    private static Purchase mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        final String paymentMethod = rs.getString("payment_method");
        final String[] parts = paymentMethod.split("\\|", 3);
        return new Purchase(
            rs.getLong("purchase_id"),
            rs.getLong("product_id"),
            rs.getLong("buyer_user_id"),
            rs.getDate("date").toLocalDate(),
            PurchaseStatus.valueOf(parts[0]),
            parts[1],
            parts[2]);
    }

    private final static RowMapper<Purchase> PURCHASE_ROW_MAPPER = PurchaseJdbcDao::mapRow;

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
    public Purchase createPurchase(Long productId, Long buyerId, Long sellerId, PurchaseStatus status, String buyerToken, String sellerToken) {
        final Map<String, Object> values = new HashMap<>();
        values.put("product_id", productId);
        values.put("buyer_user_id", buyerId);
        values.put("seller_user_id", sellerId);
        values.put("date", java.sql.Date.valueOf(LocalDate.now()));
        values.put("payment_method", encodePaymentMethod(status, buyerToken, sellerToken));
        values.put("confirmed", Boolean.FALSE);

        final Number id = jdbcInsert.executeAndReturnKey(values);

        return new Purchase(id.longValue(), productId, buyerId, LocalDate.now(), status, buyerToken, sellerToken);
    }

    @Override
    public Optional<Purchase> findById(Long purchaseId) {
        return jdbcTemplate.query("SELECT * FROM purchases WHERE purchase_id = ?", PURCHASE_ROW_MAPPER, purchaseId)
            .stream().findAny();
    }

    @Override
    public List<Purchase> findByBuyerId(Long buyerId) {
        return jdbcTemplate.query(
            "SELECT * FROM purchases WHERE buyer_user_id = ? ORDER BY date DESC",
            PURCHASE_ROW_MAPPER, buyerId);
    }

    @Override
    public List<Purchase> findBySellerId(Long sellerId) {
        return jdbcTemplate.query(
            "SELECT * FROM purchases WHERE seller_user_id = ? ORDER BY date DESC",
            PURCHASE_ROW_MAPPER, sellerId);
    }

    @Override
    public void updateStatus(Long purchaseId, PurchaseStatus status) {
        final Purchase current = findById(purchaseId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));
        jdbcTemplate.update(
            "UPDATE purchases SET payment_method = ?, confirmed = ? WHERE purchase_id = ?",
            encodePaymentMethod(status, current.getBuyerToken(), current.getSellerToken()),
            Boolean.valueOf(status == PurchaseStatus.DELIVERED),
            purchaseId);
    }
}
