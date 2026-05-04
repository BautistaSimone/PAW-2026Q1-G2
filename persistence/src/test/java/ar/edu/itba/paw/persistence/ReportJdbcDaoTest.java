package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Report;
import ar.edu.itba.paw.models.ReportedProduct;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ReportJdbcDaoTest {

    @Autowired
    private ReportJdbcDao reportDao;

    @Autowired
    private ProductJdbcDao productDao;

    @Autowired
    private UserJdbcDao userDao;

    private User createUser(final String email) {
        return userDao.createUser(email, "password", email, false, true, null, null, null, null, null, null, null, null);
    }

    private Product createProduct(final User seller, final String title, final String artist) {
        return productDao.createProduct(
            seller.getId(),
            title,
            artist,
            "Label",
            "CAT-001",
            "Argentina",
            Collections.emptyList(),
            "Description",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(1000)
        );
    }

    @Test
    public void createPersistsReportAndExistsByProductAndReporterFindsIt() {
        final User owner = createUser("report-owner@test.com");
        final User reporter = createUser("report-reporter@test.com");
        final Product product = createProduct(owner, "Reported Album", "Reported Artist");

        final Report report = reportDao.create(product.getId(), owner.getId(), reporter.getId());

        Assertions.assertNotNull(report.getReportId());
        Assertions.assertEquals(product.getId(), report.getProductId());
        Assertions.assertEquals(owner.getId(), report.getOwnerUserId());
        Assertions.assertEquals(reporter.getId(), report.getReporterUserId());
        Assertions.assertTrue(reportDao.existsByProductAndReporter(product.getId(), reporter.getId()));
        Assertions.assertFalse(reportDao.existsByProductAndReporter(product.getId(), owner.getId()));
    }

    @Test
    public void findAllGroupedByProductReturnsReportCountsAndProductMetadataOrderedByCount() {
        final User owner = createUser("report-group-owner@test.com");
        final User secondOwner = createUser("report-group-second-owner@test.com");
        final User firstReporter = createUser("report-group-reporter-a@test.com");
        final User secondReporter = createUser("report-group-reporter-b@test.com");
        final Product mostReported = createProduct(owner, "Most Reported", "Artist A");
        final Product leastReported = createProduct(secondOwner, "Least Reported", "Artist B");
        reportDao.create(mostReported.getId(), owner.getId(), firstReporter.getId());
        reportDao.create(mostReported.getId(), owner.getId(), secondReporter.getId());
        reportDao.create(leastReported.getId(), secondOwner.getId(), firstReporter.getId());

        final List<ReportedProduct> groupedReports = reportDao.findAllGroupedByProduct();

        Assertions.assertEquals(2, groupedReports.size());
        Assertions.assertEquals(mostReported.getId(), groupedReports.get(0).getProductId());
        Assertions.assertEquals(owner.getId(), groupedReports.get(0).getOwnerUserId());
        Assertions.assertEquals(2, groupedReports.get(0).getReportCount());
        Assertions.assertEquals("Most Reported", groupedReports.get(0).getProductTitle());
        Assertions.assertEquals("Artist A", groupedReports.get(0).getProductArtist());
        Assertions.assertEquals(owner.getUsername(), groupedReports.get(0).getOwnerUsername());
        Assertions.assertEquals(leastReported.getId(), groupedReports.get(1).getProductId());
        Assertions.assertEquals(1, groupedReports.get(1).getReportCount());
    }

    @Test
    public void deleteByProductIdRemovesOnlyReportsForThatProduct() {
        final User owner = createUser("report-delete-owner@test.com");
        final User reporter = createUser("report-delete-reporter@test.com");
        final Product deletedProduct = createProduct(owner, "Deleted Reports", "Artist A");
        final Product remainingProduct = createProduct(owner, "Remaining Reports", "Artist B");
        reportDao.create(deletedProduct.getId(), owner.getId(), reporter.getId());
        reportDao.create(remainingProduct.getId(), owner.getId(), reporter.getId());

        reportDao.deleteByProductId(deletedProduct.getId());

        Assertions.assertFalse(reportDao.existsByProductAndReporter(deletedProduct.getId(), reporter.getId()));
        Assertions.assertTrue(reportDao.existsByProductAndReporter(remainingProduct.getId(), reporter.getId()));
    }
}
