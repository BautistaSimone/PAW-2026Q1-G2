package ar.edu.itba.paw.persistence;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ReportJdbcDaoTest {

    @Autowired
    private ReportJdbcDao reportDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private long ownerId;
    private long reporterId;
    private long otherReporterId;
    private long productId;
    private long otherProductId;

    @BeforeEach
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        ownerId = insertUser("report-owner", "Owner");
        reporterId = insertUser("report-reporter", "Reporter");
        otherReporterId = insertUser("report-other-reporter", "Other Reporter");
        productId = insertProduct(ownerId, "Reported Album", "Reported Artist");
        otherProductId = insertProduct(ownerId, "Other Reported Album", "Other Artist");
    }

    private long insertUser(final String suffix, final String username) {
        jdbcTemplate.update(
            "INSERT INTO users (email, password, username, mod) VALUES (?, 'pass', ?, false)",
            suffix + "@test.com",
            username
        );
        return jdbcTemplate.queryForObject("CALL IDENTITY()", Long.class);
    }

    private long insertProduct(final long userId, final String title, final String artist) {
        jdbcTemplate.update(
            "INSERT INTO products (user_id, title, artist, description, sleeve_condition, record_condition, published, price) "
                + "VALUES (?, ?, ?, 'Description', 8, 9, CURRENT_DATE, 1000)",
            userId,
            title,
            artist
        );
        return jdbcTemplate.queryForObject("CALL IDENTITY()", Long.class);
    }

    @Test
    public void createPersistsReportAndExistsByProductAndReporterFindsIt() {
        final Report report = reportDao.create(productId, ownerId, reporterId);

        Assertions.assertNotNull(report.getReportId());
        Assertions.assertEquals(productId, report.getProductId());
        Assertions.assertEquals(ownerId, report.getOwnerUserId());
        Assertions.assertEquals(reporterId, report.getReporterUserId());
        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reports"));
        Assertions.assertTrue(reportDao.existsByProductAndReporter(productId, reporterId));
        Assertions.assertFalse(reportDao.existsByProductAndReporter(productId, otherReporterId));
    }

    @Test
    public void findAllGroupedByProductReturnsReportCountsAndProductMetadata() {
        reportDao.create(productId, ownerId, reporterId);
        reportDao.create(productId, ownerId, otherReporterId);
        reportDao.create(otherProductId, ownerId, reporterId);

        final List<ReportedProduct> reportedProducts = reportDao.findAllGroupedByProduct();

        Assertions.assertEquals(2, reportedProducts.size());
        final ReportedProduct mostReported = reportedProducts.get(0);
        Assertions.assertEquals(productId, mostReported.getProductId());
        Assertions.assertEquals(ownerId, mostReported.getOwnerUserId());
        Assertions.assertEquals(2, mostReported.getReportCount());
        Assertions.assertEquals("Reported Album", mostReported.getProductTitle());
        Assertions.assertEquals("Reported Artist", mostReported.getProductArtist());
        Assertions.assertEquals("Owner", mostReported.getOwnerUsername());
    }

    @Test
    public void deleteByProductIdDeletesOnlyReportsForThatProduct() {
        reportDao.create(productId, ownerId, reporterId);
        reportDao.create(productId, ownerId, otherReporterId);
        reportDao.create(otherProductId, ownerId, reporterId);

        reportDao.deleteByProductId(productId);

        Assertions.assertFalse(reportDao.existsByProductAndReporter(productId, reporterId));
        Assertions.assertFalse(reportDao.existsByProductAndReporter(productId, otherReporterId));
        Assertions.assertTrue(reportDao.existsByProductAndReporter(otherProductId, reporterId));
        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "reports"));
    }
}
